package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.ProductTemplateDocument
import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldDefinition
import com.contactfirstio.structuredproducts.data.document.TemplateCustomFieldType
import com.contactfirstio.structuredproducts.data.document.TemplateStatus
import com.contactfirstio.structuredproducts.data.repository.ProductTemplateRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

class ProductTemplateServiceTest {

    private val productTemplateRepository: ProductTemplateRepository = mock()
    private val templateAttachmentService: TemplateAttachmentService = mock()
    private val fieldCatalogService = FieldCatalogService(templateAttachmentService)
    private lateinit var productTemplateService: ProductTemplateService

    private fun validTemplateCommand(
        name: String = "PPN Template",
        description: String = "Principal protected note",
        standardFieldDefaults: Map<String, String> = requiredTemplateDefaults(),
    ): SaveProductTemplateCommand =
        SaveProductTemplateCommand(
            name = name,
            description = description,
            standardFieldDefaults = standardFieldDefaults,
        )

    private fun requiredTemplateDefaults(): Map<String, String> =
        mapOf(
            "apc_code" to "APC123",
            "pip_id" to "PIP-001",
            "risk_profile" to "Protection",
            "pay_off" to "Capital protected at maturity",
        )

    @BeforeEach
    fun setUp() {
        productTemplateService =
            ProductTemplateService(productTemplateRepository, fieldCatalogService, templateAttachmentService)
    }

    @Test
    fun `creates template with standard defaults`() {
        whenever(productTemplateRepository.save(any<ProductTemplateDocument>())).thenAnswer { invocation ->
            val document = invocation.getArgument<ProductTemplateDocument>(0)
            document.copy(id = "template-1")
        }

        val saved =
            productTemplateService.create(
                validTemplateCommand(
                    standardFieldDefaults =
                        requiredTemplateDefaults() +
                            mapOf(
                                "booking_center" to "HK/SG",
                                "product_status" to "Draft",
                            ),
                ).copy(status = TemplateStatus.ACTIVE),
            )

        assertEquals("PPN Template", saved.name)
        assertEquals(6, saved.standardFieldDefaults.size)
        assertEquals(emptySet<String>(), saved.includedCommonFieldKeys)
        assertEquals(emptySet<String>(), saved.mandatoryCommonFieldKeys)
        assertEquals(emptyList<String>(), saved.caCaaDeclarationQuestions)
        assertEquals(emptyList<TemplateCustomFieldDefinition>(), saved.customFields)
    }

    @Test
    fun `creates template with custom fields`() {
        whenever(productTemplateRepository.save(any<ProductTemplateDocument>())).thenAnswer { invocation ->
            val document = invocation.getArgument<ProductTemplateDocument>(0)
            document.copy(id = "template-1")
        }

        val saved =
            productTemplateService.create(
                validTemplateCommand().copy(
                    customFields =
                        listOf(
                            TemplateCustomFieldDefinition(
                                label = "Settlement delay",
                                dataType = TemplateCustomFieldType.INTEGER,
                                mandatory = true,
                            ),
                            TemplateCustomFieldDefinition(
                                label = "Risk tier",
                                dataType = TemplateCustomFieldType.ENUM,
                                enumOptions = listOf("Low", "Medium", "High"),
                            ),
                            TemplateCustomFieldDefinition(label = "", dataType = TemplateCustomFieldType.STRING),
                        ),
                ),
            )

        assertEquals(2, saved.customFields.size)
        assertEquals("settlement_delay", saved.customFields[0].key)
        assertEquals("Settlement delay", saved.customFields[0].label)
        assertEquals(TemplateCustomFieldType.INTEGER, saved.customFields[0].dataType)
        assertEquals(true, saved.customFields[0].mandatory)
        assertEquals("risk_tier", saved.customFields[1].key)
        assertEquals(listOf("Low", "Medium", "High"), saved.customFields[1].enumOptions)
    }

    @Test
    fun `rejects enum custom field without options`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                validTemplateCommand().copy(
                    customFields =
                        listOf(
                            TemplateCustomFieldDefinition(
                                label = "Risk tier",
                                dataType = TemplateCustomFieldType.ENUM,
                            ),
                        ),
                ),
            )
        }
    }

    @Test
    fun `rejects custom field key that conflicts with catalog field`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                validTemplateCommand().copy(
                    customFields =
                        listOf(
                            TemplateCustomFieldDefinition(
                                key = "tenor",
                                label = "Tenor override",
                                dataType = TemplateCustomFieldType.STRING,
                            ),
                        ),
                ),
            )
        }
    }

    @Test
    fun `creates template with custom CA CAA declaration questions`() {
        whenever(productTemplateRepository.save(any<ProductTemplateDocument>())).thenAnswer { invocation ->
            val document = invocation.getArgument<ProductTemplateDocument>(0)
            document.copy(id = "template-1")
        }

        val saved =
            productTemplateService.create(
                validTemplateCommand().copy(
                    caCaaDeclarationQuestions =
                        listOf(
                            "  Does the client confirm suitability?  ",
                            "",
                            "Has the brochure been read?",
                        ),
                ),
            )

        assertEquals(
            listOf(
                "Does the client confirm suitability?",
                "Has the brochure been read?",
            ),
            saved.caCaaDeclarationQuestions,
        )
    }

    @Test
    fun `rejects fixed values on system managed creation timestamp`() {
        val exception =
            assertThrows(ValidationException::class.java) {
                productTemplateService.create(
                    validTemplateCommand(
                        standardFieldDefaults =
                            requiredTemplateDefaults() +
                                mapOf("creation_timestamp" to "2026-01-01T00:00:00"),
                    ),
                )
            }
        assertEquals("Creation Timestamp is set by the system", exception.message)
    }

    @Test
    fun `rejects fixed values on system managed standard fields`() {
        val exception =
            assertThrows(ValidationException::class.java) {
                productTemplateService.create(
                    validTemplateCommand(
                        standardFieldDefaults =
                            requiredTemplateDefaults() +
                                mapOf("product_creator" to "alice@example.com"),
                    ),
                )
            }
        assertEquals("Product Creator is set by the system", exception.message)
    }

    @Test
    fun `rejects fixed values on non-fixable standard fields`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                validTemplateCommand(
                    standardFieldDefaults =
                        requiredTemplateDefaults() +
                            mapOf("product_name" to "Should not be allowed"),
                ),
            )
        }
    }

    @Test
    fun `rejects unknown common field keys`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                validTemplateCommand().copy(
                    includedCommonFieldKeys = setOf("unknown_field"),
                ),
            )
        }
    }

    @Test
    fun `rejects mandatory common field keys that are not included`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                validTemplateCommand().copy(
                    includedCommonFieldKeys = setOf("tenor"),
                    mandatoryCommonFieldKeys = setOf("currency"),
                ),
            )
        }
    }

    @Test
    fun `persists mandatory common field keys for included fields`() {
        whenever(productTemplateRepository.save(any<ProductTemplateDocument>())).thenAnswer { invocation ->
            val document = invocation.getArgument<ProductTemplateDocument>(0)
            document.copy(id = "template-1")
        }

        val saved =
            productTemplateService.create(
                validTemplateCommand().copy(
                    includedCommonFieldKeys = setOf("tenor", "currency"),
                    mandatoryCommonFieldKeys = setOf("tenor", "currency"),
                ),
            )

        assertEquals(setOf("tenor", "currency"), saved.includedCommonFieldKeys)
        assertEquals(setOf("tenor", "currency"), saved.mandatoryCommonFieldKeys)
    }

    @Test
    fun `updates existing template`() {
        val existing =
            ProductTemplateDocument(
                id = "template-1",
                name = "Old name",
                description = "",
                status = TemplateStatus.DRAFT,
                standardFieldDefaults = emptyMap(),
                includedCommonFieldKeys = emptyList(),
                mandatoryCommonFieldKeys = emptyList(),
                caCaaDeclarationQuestions = emptyList(),
                customFields = emptyList(),
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
            )
        whenever(productTemplateRepository.findById("template-1")).thenReturn(Optional.of(existing))
        whenever(productTemplateRepository.save(any<ProductTemplateDocument>())).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val updated =
            productTemplateService.update(
                "template-1",
                validTemplateCommand(
                    name = "Updated name",
                    standardFieldDefaults =
                        requiredTemplateDefaults() +
                            mapOf("market_segment" to "Public"),
                ),
            )

        assertEquals("Updated name", updated.name)
        assertEquals(
            requiredTemplateDefaults() + mapOf("market_segment" to "Public"),
            updated.standardFieldDefaults,
        )
    }

    @Test
    fun `rejects blank description`() {
        val exception =
            assertThrows(ValidationException::class.java) {
                productTemplateService.create(
                    validTemplateCommand(description = "   "),
                )
            }
        assertEquals("Description is required", exception.message)
    }

    @Test
    fun `rejects missing required template standard defaults`() {
        val exception =
            assertThrows(ValidationException::class.java) {
                productTemplateService.create(
                    validTemplateCommand(standardFieldDefaults = emptyMap()),
                )
            }
        assertEquals("Risk Profile is required", exception.message)
    }
}
