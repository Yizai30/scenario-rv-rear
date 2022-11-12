package com.example.demo.bean;

import lombok.Data;
import lombok.ToString;

import java.util.Objects;

/**
 * 需求现象 情景？
 */
@Data
@ToString(callSuper = true)
public class RequirementPhenomenon extends Phenomenon {
    private int phenomenon_requirement;
    private String phenomenon_constraint;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((phenomenon_constraint == null) ? 0 : phenomenon_constraint.hashCode());
        result = prime * result + phenomenon_requirement;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        RequirementPhenomenon rPhenomenon = (RequirementPhenomenon) obj;
        return phenomenon_requirement == rPhenomenon.phenomenon_requirement
                && Objects.equals(phenomenon_constraint, rPhenomenon.phenomenon_constraint);
    }
}
