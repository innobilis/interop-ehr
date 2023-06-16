package com.projectronin.interop.fhir.ronin.generators.resource.observation

import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.resources.ObservationGenerator
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Quantity
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.Decimal
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.ronin.generators.util.generateCode
import com.projectronin.interop.fhir.ronin.generators.util.generateExtension
import com.projectronin.interop.fhir.ronin.generators.util.generateSubject
import com.projectronin.interop.fhir.ronin.generators.util.rcdmIdentifiers
import com.projectronin.interop.fhir.ronin.generators.util.rcdmMeta
import com.projectronin.interop.fhir.ronin.profile.RoninProfile

/**
 * Helps generate ronin laboratory result  observation profile, applies meta and randomly generates an
 * acceptable code from the [possibleLaboratoryResultCodes] list, category is generated by laboratoryCategory below
 */
fun rcdmObservationLaboratoryResult(tenant: String, block: ObservationGenerator.() -> Unit): Observation {
    return observation {
        block.invoke(this)
        meta of rcdmMeta(RoninProfile.OBSERVATION_LABORATORY_RESULT, tenant) {}
        extension of generateExtension(extension.generate(), tenantSourceExtension)
        identifier of identifier.generate() + rcdmIdentifiers(tenant, identifier)
        category of listOf(
            codeableConcept {
                coding of laboratoryCategory
            }
        )
        code of generateCode(code.generate(), possibleLaboratoryResultCodes.random())
        subject of generateSubject(subject.generate(), subjectReferenceOptions)
        value of valueQuantity
    }
}

private val laboratoryCategory = listOf(
    coding {
        system of CodeSystem.OBSERVATION_CATEGORY.uri
        code of Code("laboratory")
    }
)

private val valueQuantity = DynamicValue(
    DynamicValueType.QUANTITY,
    Quantity(
        value = Decimal(68.04),
        unit = "kg".asFHIR(),
        system = CodeSystem.UCUM.uri,
        code = Code("kg")
    )
)

val possibleLaboratoryResultCodes = listOf(
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("97966-6")
        display of "European house dust mite recombinant (rDer p) 2 IgG4 Ab [Mass/volume] in Serum"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("8038-2")
        display of "Toxocara canis Ab [Units/volume] in Serum"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("51465-3")
        display of "Paragonimus sp Ab [Titer] in Pleural fluid"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("12388-5")
        display of "Phenylethylmalonamide [Mass/volume] in Serum or Plasma"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("29602-0")
        display of "Chlorphentermine [Mass/volume] in Serum or Plasma by Confirmatory method"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("94590-7")
        display of "Oncologic chromosome analysis in Tissue by Mate pair sequencing"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("100341-7")
        display of "Rubella virus IgG Ab index [Units/volume] in Serum and CSF"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("59798-9")
        display of "Ganciclovir [Mass/volume] in Plasma --peak"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("2227-7")
        display of "Enteropeptidase [Enzymatic activity/volume] in Plasma"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("73366-7")
        display of "Gabapentin induced platelet IgM Ab [Presence] in Serum or Plasma by Flow cytometry (FC)"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("10484-4")
        display of "Glial fibrillary acidic protein Ag [Presence] in Tissue by Immune stain"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("7598-6")
        display of "Perch IgG Ab [Units/volume] in Serum"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("56891-5")
        display of "CD13+HLA-DR+ cells/100 cells in Specimen"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("73240-4")
        display of "Nitroglycerin induced platelet IgM Ab [Presence] in Serum or Plasma by Flow cytometry (FC)"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("26457-2")
        display of "Erythrocytes [#/volume] in Peritoneal fluid"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("38295-2")
        display of "Methylene chloride [Mass/volume] in Water"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("14375-0")
        display of "Polymorphonuclear cells/100 leukocytes in Peritoneal fluid by Manual count"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("51431-5")
        display of "Gastrin [Mass/volume] in Serum or Plasma --pre or post XXX challenge"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("53622-7")
        display of "von Willebrand factor (vWf) cleaving protease actual/normal in Platelet poor plasma by Chromogenic method"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("93313-5")
        display of "Platelet glycoprotein disorder [Interpretation] in Blood by Flow cytometry (FC) Narrative"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("20504-7")
        display of "Histiocytes/100 cells in Cerebral spinal fluid"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("18942-3")
        display of "Meclocycline [Susceptibility]"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("4411-5")
        display of "Promazine [Mass] of Dose"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("58897-0")
        display of "Methyl ethyl ketone/Creatinine [Mass Ratio] in Urine"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("97010-3")
        display of "Clot formation kaolin+tissue factor induced [Time] in Blood"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("30564-9")
        display of "Tetradecadienoylcarnitine (C14:2) [Moles/volume] in Serum or Plasma"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("61166-5")
        display of "Hepatitis B virus codon S202G [Presence] by Genotype method"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("14459-2")
        display of "Virus identified in Penis by Culture"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("48112-7")
        display of "Glutathione.reduced [Units/mass] in Red Blood Cells"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("12528-6")
        display of "Norepinephrine [Mass/volume] in Plasma --4 hours post XXX challenge"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("51367-1")
        display of "CD79a cells/100 cells in Bone marrow"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("2279-8")
        display of "Fibrinopeptide A [Mass/volume] in Peritoneal fluid"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("34446-5")
        display of "Erythrocytes [Presence] in Body fluid"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("3466-0")
        display of "Chlorpheniramine [Mass/volume] in Serum or Plasma"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("46125-1")
        display of "Cardiolipin Ab [Presence] in Serum by Immunoassay"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("6985-6")
        display of "Beta lactamase.usual [Susceptibility]"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("47350-4")
        display of "Streptococcus pneumoniae Danish serotype 33F IgG Ab [Ratio] in Serum --2nd specimen/1st specimen"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("50363-1")
        display of "Common Pigweed IgE Ab/IgE total in Serum"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("44803-5")
        display of "GNE gene mutations found [Identifier] in Blood or Tissue by Molecular genetics method Nominal"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("80114-2")
        display of "2-Oxo-3-Hydroxy-Lysergate diethylamide [Mass/volume] in Blood by Confirmatory method"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("51953-8")
        display of "Collection date of Blood"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("12973-4")
        display of "Urea nitrogen [Mass/volume] in Peritoneal dialysis fluid --4th specimen"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("30021-0")
        display of "Borrelia burgdorferi 23kD IgM Ab [Presence] in Synovial fluid by Immunoblot"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("12462-8")
        display of "Corticotropin [Mass/volume] in Plasma --6th specimen post XXX challenge"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("22081-4")
        display of "Afipia felis IgG Ab [Titer] in Serum"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("50679-0")
        display of "First trimester maternal screen with nuchal translucency [Interpretation]"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("20612-8")
        display of "CD7 cells/100 cells in Specimen"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("47651-5")
        display of "Hydroxyproline [Moles/volume] in DBS"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("75004-2")
        display of "Food Allergen Mix fp73 (Beef+Chicken+Pork+Lamb) IgE Ab [Units/volume] in Serum by Multidisk"
    },
    coding {
        system of "http://loinc.org"
        version of "2.73"
        code of Code("40665-2")
        display of "Reticulocytes [#/volume] in Blood by Manual count"
    }
)
