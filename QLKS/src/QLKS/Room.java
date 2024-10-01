package QLKS;

public class Room {
	private int soPhong;
	private String kieuPhong;
	private double giaPhong;
	private boolean trangThaiPhong;

	public Room(int soPhong, String kieuPhong, double giaPhong, boolean trangThaiPhong) {
		this.soPhong = soPhong;
		this.kieuPhong = kieuPhong;
		this.giaPhong = giaPhong;
		this.trangThaiPhong = trangThaiPhong;
	}

	public int getSoPhong() {
		return soPhong;
	}

	public void setSoPhong(int soPhong) {
		this.soPhong = soPhong;
	}

	public String getKieuPhong() {
		return kieuPhong;
	}

	public void setKieuPhong(String kieuPhong) {
		this.kieuPhong = kieuPhong;
	}

	public double getGiaPhong() {
		return giaPhong;
	}

	public void setGiaPhong(double giaPhong) {
		this.giaPhong = giaPhong;
	}

	public boolean isTrangThaiPhong() {
		return trangThaiPhong;
	}

	public void setTrangThaiPhong(boolean trangThaiPhong) {
		this.trangThaiPhong = trangThaiPhong;
	}
}