package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.data.document.ProductTemplateDocument
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
    private val caCaaDeclarationCatalogService = CaCaaDeclarationCatalogService()
    private val fieldCatalogService = FieldCatalogService(templateAttachmentService)
    private lateinit var productTemplateService: ProductTemplateService

    @BeforeEach
    fun setUp() {
        productTemplateService =
            ProductTemplateService(
                productTemplateRepository,
                fieldCatalogService,
                templateAttachmentService,
                caCaaDeclarationCatalogService,
            )
    }

    @Test
    fun `creates template with standard defaults`() {
        whenever(productTemplateRepository.save(any<ProductTemplateDocument>())).thenAnswer { invocation ->
            val document = invocation.getArgument<ProductTemplateDocument>(0)
            document.copy(id = "template-1")
        }

        val saved =
            productTemplateService.create(
                SaveProductTemplateCommand(
                    name = "PPN Template",
                    description = "Principal protected note",
                    status = TemplateStatus.ACTIVE,
                    standardFieldDefaults =
                        mapOf(
                            "booking_center" to "HK/SG",
                            "product_status" to "Draft",
                        ),
                ),
            )

        assertEquals("PPN Template", saved.name)
        assertEquals(2, saved.standardFieldDefaults.size)
        assertEquals(emptySet<String>(), saved.includedCommonFieldKeys)
        assertEquals(emptySet<String>(), saved.includedCaCaaDeclarationQuestionKeys)
    }

    @Test
    fun `rejects fixed values on system managed standard fields`() {
        val exception =
            assertThrows(ValidationException::class.java) {
                productTemplateService.create(
                    SaveProductTemplateCommand(
                        name = "Invalid",
                        standardFieldDefaults = mapOf("product_creator" to "alice@example.com"),
                    ),
                )
            }
        assertEquals("Product Creator is set by the system", exception.message)
    }

    @Test
    fun `rejects fixed values on non-fixable standard fields`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                SaveProductTemplateCommand(
                    name = "Invalid",
                    standardFieldDefaults = mapOf("product_name" to "Should not be allowed"),
                ),
            )
        }
    }

    @Test
    fun `rejects unknown common field keys`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                SaveProductTemplateCommand(
                    name = "Invalid",
                    includedCommonFieldKeys = setOf("unknown_field"),
                ),
            )
        }
    }

    @Test
    fun `rejects unknown CA CAA declaration question keys`() {
        assertThrows(ValidationException::class.java) {
            productTemplateService.create(
                SaveProductTemplateCommand(
                    name = "Invalid",
                    includedCaCaaDeclarationQuestionKeys = setOf("unknown_question"),
                ),
            )
        }
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
                includedCaCaaDeclarationQuestionKeys = emptyList(),
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
                SaveProductTemplateCommand(
                    name = "Updated name",
                    standardFieldDefaults = mapOf("market_segment" to "Public"),
                ),
            )

        assertEquals("Updated name", updated.name)
        assertEquals(mapOf("market_segment" to "Public"), updated.standardFieldDefaults)
    }
}
