package com.meapps.cinenostalgia.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonRoleTest {
    @Test fun calculatesAgeAtMovieRelease() {
        val actor = PersonRole(1, "Actor", "Role", null, "1961-06-09", null)
        assertEquals(24, actor.ageAt("1985-07-03"))
    }

    @Test fun missingBirthdayProducesNoAge() {
        assertNull(PersonRole(1, "Actor", "Role", null, null, null).ageAt("1985-07-03"))
    }
}
