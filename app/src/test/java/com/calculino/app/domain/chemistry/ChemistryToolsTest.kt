package com.calculino.app.domain.chemistry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChemistryToolsTest {
    @Test
    fun calculatesGibbsAndSpontaneity() {
        val spontaneous = GibbsCalculator.calculate(-100.0, -200.0, 298.0)
        assertNotNull(spontaneous)
        assertTrue(spontaneous!!.deltaG < 0)
        assertEquals(Spontaneity.Spontaneous, spontaneous.spontaneity)
        val nonSpontaneous = GibbsCalculator.calculate(100.0, 50.0, 298.0)
        assertEquals(Spontaneity.NonSpontaneous, nonSpontaneous!!.spontaneity)
        assertNull(GibbsCalculator.calculate(0.0, 0.0, 0.0))
    }

    @Test
    fun findsEmpiricalAndMolecularFormula() {
        val result = EmpiricalFormulaFinder.find(mapOf("C" to 40.0, "H" to 6.67, "O" to 53.33))
        assertTrue(result is EmpiricalFormulaOutcome.Success)
        val formula = (result as EmpiricalFormulaOutcome.Success).result
        assertEquals("CH2O", formula.empiricalFormula)
        assertEquals(30.026, formula.empiricalMass, 0.01)
        val molecular = EmpiricalFormulaFinder.find(
            mapOf("C" to 40.0, "H" to 6.67, "O" to 53.33),
            180.156,
        )
        assertEquals("C6H12O6", (molecular as EmpiricalFormulaOutcome.Success).result.molecularFormula)
    }

    @Test
    fun reportsUnknownElements() {
        val result = EmpiricalFormulaFinder.find(mapOf("Xx" to 10.0))
        assertTrue(result is EmpiricalFormulaOutcome.Failure)
    }

    @Test
    fun calculatesStrongAndWeakPh() {
        val strong = PhCalculator.calculate(SolutionType.StrongAcid, 0.01, null)!!
        assertEquals(2.0, strong.ph, 1e-9)
        val base = PhCalculator.calculate(SolutionType.StrongBase, 0.01, null)!!
        assertEquals(12.0, base.ph, 1e-9)
        val weak = PhCalculator.calculate(SolutionType.WeakAcid, 0.1, 1.8e-5)!!
        assertTrue(weak.ph > 2.0 && weak.ph < 3.0)
        assertNull(PhCalculator.calculate(SolutionType.WeakAcid, 0.1, null))
        assertNull(PhCalculator.calculate(SolutionType.StrongAcid, 0.0, null))
    }
}
