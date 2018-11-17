package com.timetablegenerator.exceptions;

import com.timetablegenerator.model.Term;

public class TermScopeException extends IllegalArgumentException {

    public TermScopeException(Term subTerm, Term superTerm) {
        super(
            "Attempted to relate a term based structure (e.g. course) into a term based super structure " +
            "(e.g. a timetable) within a non-enclosing term scope (" + subTerm.getUniqueId() + " into " +
            superTerm.getUniqueId() + ")"
        );
    }
}
