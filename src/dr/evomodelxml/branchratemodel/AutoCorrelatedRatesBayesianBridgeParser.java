/*
 * MultivariateNormalDistributionModelParser.java
 *
 * Copyright (c) 2002-2019 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.evomodelxml.branchratemodel;

import dr.evomodel.branchratemodel.AutoCorrelatedBranchRatesDistribution;
import dr.evomodel.branchratemodel.shrinkage.AutoCorrelatedRatesWithBayesianBridge;
import dr.inference.distribution.shrinkage.*;
import dr.inference.model.MatrixParameter;
import dr.inference.model.Parameter;
import dr.xml.*;

@Deprecated
public class AutoCorrelatedRatesBayesianBridgeParser extends AbstractXMLObjectParser {

    private static final String BAYESIAN_BRIDGE = "autoCorrelatedRatesBayesianBridge";
    private static final String GLOBAL_SCALE = "globalScale";
    private static final String LOCAL_SCALE = "localScale";
    private static final String EXPONENT = "exponent";

    public String getParserName() {
        return BAYESIAN_BRIDGE;
    }

    public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        AutoCorrelatedBranchRatesDistribution ratesDistribution = (AutoCorrelatedBranchRatesDistribution)
                xo.getChild(AutoCorrelatedBranchRatesDistribution.class);

        XMLObject globalXo = xo.getChild(GLOBAL_SCALE);
        Parameter globalScale = (Parameter) globalXo.getChild(Parameter.class);

        Parameter localScale = null;
        if (xo.hasChildNamed(LOCAL_SCALE)) {
            XMLObject localXo = xo.getChild(LOCAL_SCALE);
            localScale = (Parameter) localXo.getChild(Parameter.class);

            if (localScale.getDimension() != ratesDistribution.getDimension()) {
                throw new XMLParseException("Local scale dimension (" + localScale.getDimension()
                        + ") != rates dimension (" + ratesDistribution.getDimension() + ")");
            }
        }

        XMLObject exponentXo = xo.getChild(EXPONENT);
        Parameter exponent = (Parameter) exponentXo.getChild(Parameter.class);

        BayesianBridgeDistributionModel distributionModel = (localScale != null) ?
                new JointBayesianBridgeDistributionModel(globalScale, localScale, exponent, 1) :
                new MarginalBayesianBridgeDistributionModel(globalScale, exponent, 1);

        return new AutoCorrelatedRatesWithBayesianBridge(ratesDistribution,
                distributionModel);
    }

    //************************************************************************
    // AbstractXMLObjectParser implementation
    //************************************************************************

    public XMLSyntaxRule[] getSyntaxRules() {
        return rules;
    }

    private final XMLSyntaxRule[] rules = {
            new ElementRule(AutoCorrelatedBranchRatesDistribution.class),
            new ElementRule(GLOBAL_SCALE,
                    new XMLSyntaxRule[]{new ElementRule(Parameter.class)}),
            new ElementRule(EXPONENT,
                    new XMLSyntaxRule[]{new ElementRule(Parameter.class)}),
            new ElementRule(LOCAL_SCALE,
                    new XMLSyntaxRule[]{new ElementRule(MatrixParameter.class)}, true),
    };

    public String getParserDescription() {
        return "Describes a scaled mixture of normals distribution with a given global and local scale " +
                "that can be used in a distributionLikelihood element";
    }

    public Class getReturnType() {
        return AutoCorrelatedRatesWithBayesianBridge.class;
    }
}
