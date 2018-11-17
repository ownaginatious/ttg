package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.Department;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class DepartmentTests {

    @Test
    public void creation() {
        Department d = Department.of("DEP", "Department");
        assertEquals("DEP", d.getCode());
        assertEquals("Department", d.getName());
    }

    @Test
    public void compare(){
        Department d1 = Department.of("A", "Department A");
        Department d2 = Department.of("B", "Department B");
        Department d3 = Department.of("B", "Department A");

        List<Department> departments = Arrays.asList(d2, d1, d3, d1);
        Collections.sort(departments);

        assertEquals(Arrays.asList(d1, d1, d3, d2), departments);
    }

}