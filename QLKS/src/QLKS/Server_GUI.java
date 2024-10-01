package QLKS;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server_GUI extends JFrame {
	private JTable table_DSPhong;
	private ServerSocket serverSocket;

	public Server_GUI() {
		setTitle("Quản lý khách sạn - Server");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		table_DSPhong = new JTable(
				new DefaultTableModel(new Object[] { "Số phòng", "Kiểu phòng", "Giá phòng", "Trạng thái" }, 0));
		JScrollPane scrollPane = new JScrollPane(table_DSPhong);
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton addRoomButton = new JButton("Thêm phòng");
		JButton updateRoomButton = new JButton("Cập nhật phòng");
		JButton deleteRoomButton = new JButton("Xóa phòng");

		addRoomButton.addActionListener(e -> addRoom());
		updateRoomButton.addActionListener(e -> updateRoom());
		deleteRoomButton.addActionListener(e -> deleteRoom());

		buttonPanel.add(addRoomButton);
		buttonPanel.add(updateRoomButton);
		buttonPanel.add(deleteRoomButton);

		add(buttonPanel, BorderLayout.SOUTH);

		loadRoomData();

		new Thread(this::startServer).start();
	}

	private void loadRoomData() {
		DefaultTableModel model = (DefaultTableModel) table_DSPhong.getModel();
		model.setRowCount(0);

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM Phong")) {

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

	private void addRoom() {
		JTextField soPhongField = new JTextField();
		JTextField kieuPhongField = new JTextField();
		JTextField giaPhongField = new JTextField();
		JComboBox<String> trangThaiField = new JComboBox<>(new String[] { "Sẵn sàng", "Không sẵn sàng" });

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel("Số phòng:"));
		panel.add(soPhongField);
		panel.add(new JLabel("Kiểu phòng:"));
		panel.add(kieuPhongField);
		panel.add(new JLabel("Giá phòng:"));
		panel.add(giaPhongField);
		panel.add(new JLabel("Trạng thái:"));
		panel.add(trangThaiField);

		int result = JOptionPane.showConfirmDialog(null, panel, "Thêm phòng mới", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			try {
				int soPhong = Integer.parseInt(soPhongField.getText());
				String kieuPhong = kieuPhongField.getText();
				double giaPhong = Double.parseDouble(giaPhongField.getText());
				boolean trangThai = trangThaiField.getSelectedItem().equals("Sẵn sàng");

				try (Connection conn = DatabaseConnection.getConnection();
						PreparedStatement pstmt = conn.prepareStatement(
								"INSERT INTO Phong (SoPhong, KieuPhong, GiaPhong, TrangThaiPhong) VALUES (?, ?, ?, ?)")) {
					pstmt.setInt(1, soPhong);
					pstmt.setString(2, kieuPhong);
					pstmt.setDouble(3, giaPhong);
					pstmt.setBoolean(4, trangThai);
					pstmt.executeUpdate();
					JOptionPane.showMessageDialog(this, "Phòng đã được thêm thành công.");
					loadRoomData();
				}
			} catch (NumberFormatException | SQLException ex) {
				JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
			}
		}
	}

	private void updateRoom() {
		int selectedRow = table_DSPhong.getSelectedRow();
		if (selectedRow >= 0) {
			int soPhong = (int) table_DSPhong.getValueAt(selectedRow, 0);
			String kieuPhong = (String) table_DSPhong.getValueAt(selectedRow, 1);
			double giaPhong = (double) table_DSPhong.getValueAt(selectedRow, 2);
			boolean trangThai = table_DSPhong.getValueAt(selectedRow, 3).equals("Sẵn sàng");

			JTextField kieuPhongField = new JTextField(kieuPhong);
			JTextField giaPhongField = new JTextField(String.valueOf(giaPhong));
			JComboBox<String> trangThaiField = new JComboBox<>(new String[] { "Sẵn sàng", "Không sẵn sàng" });
			trangThaiField.setSelectedItem(trangThai ? "Sẵn sàng" : "Không sẵn sàng");

			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel("Kiểu phòng:"));
			panel.add(kieuPhongField);
			panel.add(new JLabel("Giá phòng:"));
			panel.add(giaPhongField);
			panel.add(new JLabel("Trạng thái:"));
			panel.add(trangThaiField);

			int result = JOptionPane.showConfirmDialog(null, panel, "Cập nhật phòng", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				try {
					String newKieuPhong = kieuPhongField.getText();
					double newGiaPhong = Double.parseDouble(giaPhongField.getText());
					boolean newTrangThai = trangThaiField.getSelectedItem().equals("Sẵn sàng");

					try (Connection conn = DatabaseConnection.getConnection();
							PreparedStatement pstmt = conn.prepareStatement(
									"UPDATE Phong SET KieuPhong = ?, GiaPhong = ?, TrangThaiPhong = ? WHERE SoPhong = ?")) {
						pstmt.setString(1, newKieuPhong);
						pstmt.setDouble(2, newGiaPhong);
						pstmt.setBoolean(3, newTrangThai);
						pstmt.setInt(4, soPhong);
						pstmt.executeUpdate();
						JOptionPane.showMessageDialog(this, "Phòng đã được cập nhật thành công.");
						loadRoomData();
					}
				} catch (NumberFormatException | SQLException ex) {
					JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng để cập nhật.");
		}
	}

	private void deleteRoom() {
		int selectedRow = table_DSPhong.getSelectedRow();
		if (selectedRow >= 0) {
			int soPhong = (int) table_DSPhong.getValueAt(selectedRow, 0);
			int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phòng " + soPhong + "?",
					"Xác nhận xóa", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				try (Connection conn = DatabaseConnection.getConnection();
						PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Phong WHERE SoPhong = ?")) {
					pstmt.setInt(1, soPhong);
					int affectedRows = pstmt.executeUpdate();
					if (affectedRows > 0) {
						JOptionPane.showMessageDialog(this, "Phòng đã được xóa thành công.");
						loadRoomData();
					} else {
						JOptionPane.showMessageDialog(this, "Không tìm thấy phòng để xóa.");
					}
				} catch (SQLException ex) {
					JOptionPane.showMessageDialog(this, "Lỗi khi xóa phòng: " + ex.getMessage());
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng để xóa.");
		}
	}

	private void startServer() {
		try {
			serverSocket = new ServerSocket(1234);
			System.out.println("Server is listening on port 1234");

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");
				new ClientHandler(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class ClientHandler extends Thread {
		private Socket socket;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {

				int roomNumber = input.readInt();
				boolean roomAvailable = checkRoomAvailability(roomNumber);

				if (roomAvailable) {
					int choice = JOptionPane.showConfirmDialog(null,
							"Khách hàng muốn thuê phòng " + roomNumber + ". Bạn có đồng ý?", "Thuê phòng",
							JOptionPane.OK_CANCEL_OPTION);
					if (choice == JOptionPane.OK_OPTION) {
						if (rentRoom(roomNumber)) {
							output.writeObject("Thuê phòng thành công");
							loadRoomData();
						} else {
							output.writeObject("Thuê phòng thất bại");
						}
					} else {
						output.writeObject("Thuê phòng thất bại");
					}
				} else {
					output.writeObject("Phòng không sẵn sàng cho thuê");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private boolean checkRoomAvailability(int roomNumber) {
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement pstmt = conn
							.prepareStatement("SELECT TrangThaiPhong FROM Phong WHERE SoPhong = ?")) {
				pstmt.setInt(1, roomNumber);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					return rs.getBoolean("TrangThaiPhong");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		private boolean rentRoom(int roomNumber) {
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement pstmt = conn
							.prepareStatement("UPDATE Phong SET TrangThaiPhong = 0 WHERE SoPhong = ?")) {
				pstmt.setInt(1, roomNumber);
				int affectedRows = pstmt.executeUpdate();
				return affectedRows > 0;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Server_GUI().setVisible(true));
	}
}