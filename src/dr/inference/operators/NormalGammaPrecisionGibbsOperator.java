/*
 * NormalGammaPrecisionGibbsOperator.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
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

package dr.inference.operators;

import dr.evomodel.treedatalikelihood.TreeDataLikelihood;
import dr.evomodel.treedatalikelihood.preorder.ModelExtensionProvider;
import dr.inference.distribution.DistributionLikelihood;
import dr.inference.distribution.GammaDistributionModel;
import dr.inference.distribution.LogNormalDistributionModel;
import dr.inference.distribution.NormalDistributionModel;
import dr.inference.model.Parameter;
import dr.inference.operators.repeatedMeasures.GammaGibbsProvider;
import dr.math.MathUtils;
import dr.math.distributions.Distribution;
import dr.math.distributions.GammaDistribution;
import dr.math.matrixAlgebra.Vector;
import dr.xml.*;

/**
 * @author Marc A. Suchard
 * @author Philippe Lemey
 */
public class NormalGammaPrecisionGibbsOperator extends SimpleMCMCOperator implements GibbsOperator, Reportable {

    public static final String OPERATOR_NAME = "normalGammaPrecisionGibbsOperator";
    public static final String LIKELIHOOD = "likelihood";
    private static final String NORMAL_EXTENSION = "normalExtension";
    public static final String PRIOR = "prior";
    private static final String WORKING = "workingDistribution";
    private static final String TREE_TRAIT_NAME = "treeTraitName";

    public NormalGammaPrecisionGibbsOperator(GammaGibbsProvider gammaGibbsProvider, Distribution prior,
                                             double weight) {
        this(gammaGibbsProvider, prior, null, weight);
    }

    public NormalGammaPrecisionGibbsOperator(GammaGibbsProvider gammaGibbsProvider,
                                             Distribution prior, Distribution working,
                                             double weight) {
        this.gammaGibbsProvider = gammaGibbsProvider;
        this.precisionParameter = gammaGibbsProvider.getPrecisionParameter();

        this.priorParametrization = new GammaParametrization(
                prior.mean(), prior.variance());

        if (working != null) {
            this.workingParametrization = new GammaParametrization(
                    working.mean(), working.variance());
        } else {
            this.workingParametrization = null;
        }

        setWeight(weight);
    }

    /**
     * @return a short descriptive message of the performance of this operator.
     */
    public String getPerformanceSuggestion() {
        return null;
    }

    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public String getReport() {
        int dimTrait = precisionParameter.getDimension();
        double[] obsCounts = new double[dimTrait];
        double[] sumSquaredErrors = new double[dimTrait];

        gammaGibbsProvider.drawValues();

        for (int i = 0; i < dimTrait; i++) {
            final GammaGibbsProvider.SufficientStatistics statistics = gammaGibbsProvider.getSufficientStatistics(i);
            obsCounts[i] = statistics.observationCount;
            sumSquaredErrors[i] = statistics.sumOfSquaredErrors;
        }

        StringBuilder sb = new StringBuilder(OPERATOR_NAME + " report:\n");
        sb.append("Observation counts:\t");
        sb.append(new Vector(obsCounts));
        sb.append("\n");
        sb.append("Sum of squared errors:\t");
        sb.append(new Vector(sumSquaredErrors));
        return sb.toString();
    }

    static class GammaParametrization {
        private final double rate;
        private final double shape;

        GammaParametrization(double mean, double variance) {
            if (mean == 0) {
                rate = 0;
                shape = -0.5; // Uninformative prior
            } else {
                rate = mean / variance;
                shape = mean * rate;
            }
        }

        double getRate() {
            return rate;
        }

        double getShape() {
            return shape;
        }
    }

    private double weigh(double working, double prior) {
        return (1.0 - pathParameter) * working + pathParameter * prior;
    }

    public double doOperation() {

        gammaGibbsProvider.drawValues();

        for (int dim = 0; dim < precisionParameter.getDimension(); ++dim) {

            final GammaGibbsProvider.SufficientStatistics statistics = gammaGibbsProvider.getSufficientStatistics(dim);

            double shape = pathParameter * statistics.observationCount / 2;
            double rate = pathParameter * statistics.sumOfSquaredErrors / 2;

            if (workingParametrization == null) {

                shape += priorParametrization.getShape();
                rate += priorParametrization.getRate();

            } else {

                shape += weigh(priorParametrization.getShape(), priorParametrization.getShape());
                rate += weigh(priorParametrization.getRate(), priorParametrization.getShape());

            }

            final double draw = MathUtils.nextGamma(shape, rate); // Gamma( \alpha + n/2 , \beta + (1/2)*SSE )

            precisionParameter.setParameterValue(dim, draw);
        }

        return 0;
    }

    @Override
    public void setPathParameter(double beta) {
        if (beta < 0.0 || beta > 1.0) {
            throw new IllegalArgumentException("Invalid pathParameter value");
        }

        this.pathParameter = beta;
    }

    /**
     * @return the number of steps the operator performs in one go.
     */
    public int getStepCount() {
        return 1;
    }

    public static dr.xml.XMLObjectParser PARSER = new dr.xml.AbstractXMLObjectParser() {

        public String getParserName() {
            return OPERATOR_NAME;
        }

        private void checkGammaDistribution(DistributionLikelihood distribution) throws XMLParseException {
            if (!((distribution.getDistribution() instanceof GammaDistribution) ||
                    (distribution.getDistribution() instanceof GammaDistributionModel))) {
                throw new XMLParseException("Gibbs operator assumes normal-gamma model");
            }
        }

        public Object parseXMLObject(XMLObject xo) throws XMLParseException {

            final double weight = xo.getDoubleAttribute(WEIGHT);
            final DistributionLikelihood prior = (DistributionLikelihood) xo.getElementFirstChild(PRIOR);

            checkGammaDistribution(prior);

            final DistributionLikelihood working = (xo.hasChildNamed(WORKING) ?
                    (DistributionLikelihood) xo.getElementFirstChild(WORKING) :
                    null);

            Distribution workingDistribution = null;
            if (working != null) {
                checkGammaDistribution(working);
                workingDistribution = working.getDistribution();
            }

            final GammaGibbsProvider gammaGibbsProvider;

            if (xo.hasChildNamed(LIKELIHOOD)) {

                DistributionLikelihood likelihood = (DistributionLikelihood) xo.getElementFirstChild(LIKELIHOOD);

                if (!((likelihood.getDistribution() instanceof NormalDistributionModel) ||
                        (likelihood.getDistribution() instanceof LogNormalDistributionModel)
                )) {
                    throw new XMLParseException("Gibbs operator assumes normal-gamma model");
                }

                gammaGibbsProvider = new GammaGibbsProvider.Default(likelihood);

            } else {

                XMLObject cxo = xo.getChild(NORMAL_EXTENSION);

                ModelExtensionProvider.NormalExtensionProvider dataModel = (ModelExtensionProvider.NormalExtensionProvider)
                        cxo.getChild(ModelExtensionProvider.NormalExtensionProvider.class);

                TreeDataLikelihood likelihood = (TreeDataLikelihood) cxo.getChild(TreeDataLikelihood.class);

                String treeTraitName = cxo.getStringAttribute(TREE_TRAIT_NAME);

                gammaGibbsProvider = new GammaGibbsProvider.NormalExtensionGibbsProvider(
                        dataModel, likelihood, treeTraitName);
            }

            return new NormalGammaPrecisionGibbsOperator(gammaGibbsProvider,
                    prior.getDistribution(), workingDistribution,
                    weight);
        }

        //************************************************************************
        // AbstractXMLObjectParser implementation
        //************************************************************************

        public String getParserDescription() {
            return "This element returns a operator on the precision parameter of a normal model with gamma prior.";
        }

        public Class getReturnType() {
            return MCMCOperator.class;
        }

        public XMLSyntaxRule[] getSyntaxRules() {
            return rules;
        }

        private final XMLSyntaxRule[] rules = {
                AttributeRule.newDoubleRule(WEIGHT),
                new XORRule(
                        new ElementRule(LIKELIHOOD,
                                new XMLSyntaxRule[]{
                                        new ElementRule(DistributionLikelihood.class)
                                }),

                        new ElementRule(NORMAL_EXTENSION,
                                new XMLSyntaxRule[]{
                                        new ElementRule(ModelExtensionProvider.NormalExtensionProvider.class),
                                        new ElementRule(TreeDataLikelihood.class),
                                        AttributeRule.newStringRule(TREE_TRAIT_NAME)
                                })

                ),
                new ElementRule(PRIOR,
                        new XMLSyntaxRule[]{
                                new ElementRule(DistributionLikelihood.class)
                        }),
                new ElementRule(WORKING,
                        new XMLSyntaxRule[]{
                                new ElementRule(DistributionLikelihood.class)
                        }, true),
        };
    };

    private final GammaGibbsProvider gammaGibbsProvider;
    private final Parameter precisionParameter;

    private final GammaParametrization priorParametrization;
    private final GammaParametrization workingParametrization;

    private double pathParameter = 1.0;
}
