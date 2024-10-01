package QLKS;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.HashSet;

public class Client_GUI extends JFrame {
	private JTable table_DSPhong;
	private JTextField txt_SoPhong;
	private JComboBox<String> cB_KieuPhong;
	private JTextField txt_GiaPhong;
	private JComboBox<String> cB_TinhTrangPhong;
	private JButton btn_TimPhong;
	private JButton btn_ThuePhong;
	private JButton btn_Reload;

	public Client_GUI() {
		setTitle("Quản lý khách sạn - Client");
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		table_DSPhong = new JTable(
				new DefaultTableModel(new Object[] { "Số phòng", "Kiểu phòng", "Giá phòng", "Trạng thái" }, 0));
		JScrollPane scrollPane = new JScrollPane(table_DSPhong);
		add(scrollPane, BorderLayout.CENTER);

		JPanel panel = new JPanel(new GridLayout(4, 2));
		panel.add(new JLabel("Số phòng:"));
		txt_SoPhong = new JTextField();
		panel.add(txt_SoPhong);

		panel.add(new JLabel("Kiểu phòng:"));
		cB_KieuPhong = new JComboBox<>();
		loadRoomTypes();
		panel.add(cB_KieuPhong);

		panel.add(new JLabel("Giá phòng:"));
		txt_GiaPhong = new JTextField();
		panel.add(txt_GiaPhong);

		panel.add(new JLabel("Trạng thái:"));
		cB_TinhTrangPhong = new JComboBox<>(new String[] { "Tất cả", "Sẵn sàng", "Không sẵn sàng" });
		panel.add(cB_TinhTrangPhong);

		add(panel, BorderLayout.NORTH);

		JPanel btnPanel = new JPanel();
		btn_TimPhong = new JButton("Tìm phòng");
		btn_TimPhong.addActionListener(e -> loadFilteredRooms());
		btnPanel.add(btn_TimPhong);

		btn_ThuePhong = new JButton("Thuê phòng");
		btn_ThuePhong.addActionListener(e -> rentRoom());
		btnPanel.add(btn_ThuePhong);

		btn_Reload = new JButton("Tải lại");
		btn_Reload.addActionListener(e -> reloadGUI());
		btnPanel.add(btn_Reload);

		add(btnPanel, BorderLayout.SOUTH);

		loadFilteredRooms();
	}

	private void loadRoomTypes() {
		cB_KieuPhong.removeAllItems();
		cB_KieuPhong.addItem("Tất cả");

		HashSet<String> roomTypes = new HashSet<>();

		String query = "SELECT DISTINCT KieuPhong FROM Phong";

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			while (rs.next()) {
				String kieuPhong = rs.getString("KieuPhong");
				if (roomTypes.add(kieuPhong)) {
					cB_KieuPhong.addItem(kieuPhong);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Lỗi khi tải kiểu phòng: " + e.getMessage());
		}
	}

	private void loadFilteredRooms() {
		DefaultTableModel model = (DefaultTableModel) table_DSPhong.getModel();
		model.setRowCount(0);

		String soPhongFilter = txt_SoPhong.getText().trim();
		String kieuPhongFilter = (String) cB_KieuPhong.getSelectedItem();
		String giaPhongFilter = txt_GiaPhong.getText().trim();
		String trangThaiFilter = (String) cB_TinhTrangPhong.getSelectedItem();

		StringBuilder query = new StringBuilder("SELECT * FROM Phong WHERE 1=1");

		if (!soPhongFilter.isEmpty()) {
			query.append(" AND SoPhong LIKE '%").append(soPhongFilter).append("%'");
		}

		if (!kieuPhongFilter.equals("Tất cả")) {
			query.append(" AND KieuPhong LIKE N'").append(kieuPhongFilter).append("'");
		}

		if (!giaPhongFilter.isEmpty()) {
			query.append(" AND GiaPhong <= ").append(giaPhongFilter);
		}

		if (!trangThaiFilter.equals("Tất cả")) {
			boolean trangThai = trangThaiFilter.equals("Sẵn sàng");
			query.append(" AND TrangThaiPhong = ").append(trangThai ? "1" : "0");
		}

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query.toString())) {

			while (rs.next()) {
				int soPhong = rs.getInt("SoPhong");
				String kieuPhong = rs.getString("KieuPhong");
				double giaPhong = rs.getDouble("GiaPhong");
				boolean trangThai = rs.getBoolean("TrangThaiPhong");
				model.addRow(new Object[] { soPhong, kieuPhong, giaPhong, trangThai ? "Sẵn sàng" : "Không sẵn sàng" });
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phòng: " + e.getMessage());
		}
	}

	private void rentRoom() {
		int selectedRow = table_DSPhong.getSelectedRow();
		if (selectedRow >= 0) {
			int roomNumber = (int) table_DSPhong.getValueAt(selectedRow, 0);
			try (Socket socket = new Socket("localhost", 1234);
					ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

				output.writeInt(roomNumber);
				output.flush();

				String response = (String) input.readObject();
				JOptionPane.showMessageDialog(this, response);

				if (response.equals("Thuê phòng thành công") || response.equals("Thuê phòng thất bại") || response.equals("Phòng không sẵn sàng cho thuê")) {
					loadFilteredRooms();
					loadRoomTypes();
				}

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error communicating with server: " + e.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để thuê.");
		}
	}

	private void reloadGUI() {
		loadRoomTypes();
		loadFilteredRooms();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Client_GUI().setVisible(true));
	}
}
