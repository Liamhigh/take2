package org.verumomnis.forensic.engine

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ContradictionEngine
 */
class ContradictionEngineTest {
    
    @Test
    fun testDetectContradictions_didAndDidNot() {
        val engine = ContradictionEngine()
        val statements = listOf(
            "He did attend the meeting",
            "He did not attend the meeting"
        )
        
        val contradictions = engine.detectContradictions(statements)
        
        assertEquals(1, contradictions.size)
        assertEquals("He did attend the meeting", contradictions[0].statementA)
        assertEquals("He did not attend the meeting", contradictions[0].statementB)
    }
    
    @Test
    fun testDetectContradictions_neverAndDid() {
        val engine = ContradictionEngine()
        val statements = listOf(
            "I never went there",
            "I did go there yesterday"
        )
        
        val contradictions = engine.detectContradictions(statements)
        
        assertEquals(1, contradictions.size)
    }
    
    @Test
    fun testDetectContradictions_alwaysAndNever() {
        val engine = ContradictionEngine()
        val statements = listOf(
            "I always lock the door",
            "I never lock the door"
        )
        
        val contradictions = engine.detectContradictions(statements)
        
        assertEquals(1, contradictions.size)
    }
    
    @Test
    fun testDetectContradictions_wasAndWasNot() {
        val engine = ContradictionEngine()
        val statements = listOf(
            "The door was locked",
            "The door was not locked"
        )
        
        val contradictions = engine.detectContradictions(statements)
        
        assertEquals(1, contradictions.size)
    }
    
    @Test
    fun testDetectContradictions_noContradictions() {
        val engine = ContradictionEngine()
        val statements = listOf(
            "The sky is blue",
            "The grass is green"
        )
        
        val contradictions = engine.detectContradictions(statements)
        
        assertEquals(0, contradictions.size)
    }
}
