package synchropather.systems.state;

public class SpecimenCoordinates{

	public final double[] xyz;
	//TODO another xyz to show orientation? Or more generally a 3x4 affine transform matrix.

	public SpecimenCoordinates(double x, double y, double z){
		this(new double[]{x,y,z});
	}

	public SpecimenCoordinates(double[] xyz){
		this.xyz = xyz;
	}

	@Override
	public String toString() {
		return String.format(
				"SpecimenCoordinates(x=%.2f, y=%.2f, z=%.2f)",
				xyz[0], xyz[1], xyz[2]
		);
	}
	
	//forkEdits, doesnt modify this. TODO in case more fields than xyz are added later, keep the other fields.
	public SpecimenCoordinates moveTo(double[] xyz){
		return new SpecimenCoordinates(xyz);
	}


}