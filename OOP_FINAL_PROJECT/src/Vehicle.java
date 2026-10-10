public class Vehicle {

    protected String vehicleId;
    protected String brand;
    protected String model;
    protected String type;
    protected String plateNumber;
    protected double rentalRate;
    protected String availability;
    protected double mileage;

    public Vehicle(){
        super();
    }


    public Vehicle(String brand, String vehicleId, String model, String type, String plateNumber, double rentalRate, String availability, double mileage) {
        this.brand = brand;
        this.vehicleId = vehicleId;
        this.model = model;
        this.type = type;
        this.plateNumber = plateNumber;
        this.rentalRate = rentalRate;
        this.availability = availability;
        this.mileage = mileage;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public double getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(double rentalRate) {
        this.rentalRate = rentalRate;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public double calculateRentalRate(){
        return 15000.00;
    }

    public void displayDetails(){
        System.out.print("Brand of the Vehicle: " + getBrand()  );
        System.out.print("Id of the Vehicle: " + getVehicleId());
        System.out.print("Model of the Vehicle: " + getModel());
        System.out.print("Type of the Vehicle: " + getType());
        System.out.print("Plate Number of the Vehicle: " + getPlateNumber());
        System.out.print("Rental Rate of the Vehicle: " + getRentalRate());
        System.out.print("Availability of the Vehicle: " + getAvailability());
        System.out.print("Mileage Level of the Vehicle: "+ getMileage());

    }




}
