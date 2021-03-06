/* DO NOT EDIT THIS FILE - it is machine generated */
/* Generated for class dr.evomodel.treelikelihood.NativeNucleotideLikelihoodCore */
/* Generated from NativeNucleotideLik%231272A6.java*/

#ifndef _Included_dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore
#define _Included_dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif

/**
	 NativeNucleotideLikelihoodCore - An implementation of LikelihoodCore for nucleotides 
	 that calls native methods for maximum speed. The native methods should be in
	 a shared library called "NucleotideLikelihoodCore" but the exact name will be system
	 dependent (i.e. "libNucleotideLikelihoodCore.so" or "NucleotideLikelihoodCore.dll").
	 @version $Id: NucleotideLikelihoodCore.h,v 1.2 2003/10/28 16:26:31 rambaut Exp $
	 @author Andrew Rambaut
*/

/*
 * Class:     dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore
 * Method:    nativeStatesStatesPruning
 * Signature: ([I[D[I[DII[D)V
 */

/**
	 Calculates partial likelihoods at a node when both children have states.
*/

JNIEXPORT void JNICALL Java_dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore_nativeStatesStatesPruning
	(JNIEnv *, jobject, jintArray, jdoubleArray, jintArray, jdoubleArray, jint, jint, jdoubleArray);

/*
 * Class:     dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore
 * Method:    nativeStatesPartialsPruning
 * Signature: ([I[D[D[DII[D)V
 */
JNIEXPORT void JNICALL Java_dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore_nativeStatesPartialsPruning
	(JNIEnv *, jobject, jintArray, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jdoubleArray);

/*
 * Class:     dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore
 * Method:    nativePartialsPartialsPruning
 * Signature: ([D[D[D[DII[D)V
 */
JNIEXPORT void JNICALL Java_dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore_nativePartialsPartialsPruning
	(JNIEnv *, jobject, jdoubleArray, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jdoubleArray);

/*
 * Class:     dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore
 * Method:    nativeIntegratePartials
 * Signature: ([D[DII[D)V
 */
JNIEXPORT void JNICALL Java_dr_evomodel_treelikelihood_NativeNucleotideLikelihoodCore_nativeIntegratePartials
	(JNIEnv *, jobject, jdoubleArray, jdoubleArray, jint, jint, jdoubleArray);

#ifdef __cplusplus
}
#endif
#endif
