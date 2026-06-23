package com.contactfirstio.structuredproducts.service

import com.contactfirstio.structuredproducts.catalog.CatalogFieldDefinition
import com.contactfirstio.structuredproducts.data.document.FieldDataType
import com.contactfirstio.structuredproducts.data.document.FieldKind
import com.contactfirstio.structuredproducts.data.document.FieldRequirement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class FieldCatalogServiceTest {

    private val attachmentValidator: AttachmentValidator = mock()
    private lateinit var fieldCatalogService: FieldCatalogService

    @BeforeEach
    fun setUp() {
        fieldCatalogService = FieldCatalogService(attachmentValidator)
    }

    @Test
    fun `validateTemplateForm reports missing metadata and required standard fields`() {
        val validation =
            fieldCatalogService.validateTemplateForm(
                SaveProductTemplateCommand(name = "", description = ""),
            )

        assertFalse(validation.isValid)
        assertTrue(validation.missingFieldLabels.contains("Template name"))
        assertTrue(validation.missingFieldLabels.contains("Description"))
        assertTrue(validation.missingFieldLabels.contains("Risk Profile"))
        assertTrue(validation.hasStandardFieldErrors)
    }

    @Test
    fun `validateTemplateForm passes when required fields are present`() {
        val validation =
            fieldCatalogService.validateTemplateForm(
                SaveProductTemplateCommand(
                    name = "Template",
                    description = "Description",
                    standardFieldDefaults =
                        mapOf(
                            "apc_code" to "APC123",
                            "pip_id" to "PIP-001",
                            "risk_profile" to "Protection",
                            "pay_off" to "Capital protected at maturity",
                        ),
                ),
            )

        assertTrue(validation.isValid)
    }

    @Test
    fun `suggestedDefaultsForNewTemplate includes known defaults`() {
        val defaults = fieldCatalogService.suggestedDefaultsForNewTemplate()

        assertEquals("Draft", defaults["product_status"])
        assertEquals("HK/SG", defaults["booking_center"])
        assertEquals("Institutional", defaults["market_segment"])
        assertEquals("Cash", defaults["itm_settlement_type"])
        assertEquals("Notional", defaults["investment_amount"])
        assertTrue(defaults.containsKey("effective_from"))
        assertTrue(defaults.containsKey("effective_to"))
    }

    @Test
    fun `validateDefaultValue accepts boolean enum options case-insensitively`() {
        val field =
            CatalogFieldDefinition(
                key = "sample_flag",
                category = "Test",
                displayName = "Sample Flag",
                dataType = FieldDataType.BOOLEAN,
                requirement = FieldRequirement.OPTIONAL,
                enumOptions = listOf("Yes", "No"),
                kind = FieldKind.STANDARD,
            )

        fieldCatalogService.validateDefaultValue(field, "yes")
        fieldCatalogService.validateDefaultValue(field, "NO")
    }

    @Test
    fun `validateDefaultValue rejects invalid boolean enum option`() {
        val field =
            CatalogFieldDefinition(
                key = "sample_flag",
                category = "Test",
                displayName = "Sample Flag",
                dataType = FieldDataType.BOOLEAN,
                requirement = FieldRequirement.OPTIONAL,
                enumOptions = listOf("True", "False"),
                kind = FieldKind.COMMON,
            )

        assertThrows(ValidationException::class.java) {
            fieldCatalogService.validateDefaultValue(field, "Maybe")
        }
    }

    @Test
    fun `validateDefaultValue validates file list attachments through port`() {
        val field =
            CatalogFieldDefinition(
                key = "brochure_attachments",
                category = "Test",
                displayName = "Brochure Attachments",
                dataType = FieldDataType.FILE_LIST,
                requirement = FieldRequirement.OPTIONAL,
                kind = FieldKind.STANDARD,
            )

        fieldCatalogService.validateDefaultValue(field, "attachment-1,attachment-2")

        verify(attachmentValidator).validateAttachmentIds("attachment-1,attachment-2")
    }

    @Test
    fun `validateMandatoryCommonKeys rejects mandatory fields that are not included`() {
        assertThrows(ValidationException::class.java) {
            fieldCatalogService.validateMandatoryCommonKeys(
                mandatoryKeys = setOf("tenor"),
                includedKeys = emptySet(),
            )
        }
    }

    @Test
    fun `parse and serialize checkbox group values`() {
        assertEquals(setOf("Notional", "Client Proceed"), fieldCatalogService.parseCheckboxGroupValue("Client Proceed, Notional"))
        assertEquals("Client Proceed,Notional", fieldCatalogService.serializeCheckboxGroupValue(setOf("Notional", "Client Proceed")))
    }

    @Test
    fun `validateDefaultValue skips blank values`() {
        val field =
            CatalogFieldDefinition(
                key = "amount",
                category = "Test",
                displayName = "Amount",
                dataType = FieldDataType.DOUBLE,
                requirement = FieldRequirement.OPTIONAL,
                kind = FieldKind.STANDARD,
            )

        fieldCatalogService.validateDefaultValue(field, "   ")

        verifyNoInteractions(attachmentValidator)
    }
}
