package dr.inference.hmc;

import dr.math.matrixAlgebra.ReadableVector;
import dr.math.matrixAlgebra.WrappedVector;
/**
 * @author Zhenyu Zhang
 */

public interface ReversibleHMCProvider {

    void reversiblePositionMomentumUpdate(WrappedVector position, WrappedVector momentum, int direction, double time);

    WrappedVector drawMomentum();

    double getKineticEnergy(ReadableVector momentum);
}