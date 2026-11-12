package com.smartphonestore.manager;

import com.smartphonestore.model.auth.AdminStaff;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.auth.SalesStaff;
import com.smartphonestore.model.auth.WarehouseStaff;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.FileIO;
import java.util.ArrayList;
import java.util.List;

public class EmployeeManager {
    private static final String FILE_PATH = "data/employees.txt";
    private final List<Employee> employees = new ArrayList<>();

    public EmployeeManager() {
        loadFromFile();
    }

    // ===================== ĐỌC / GHI FILE =====================
    // Format: id|fullName|phoneNumber|baseSalary|role|hireYear|extra...
    //   ADMIN     -> 7 cột: ...|managementBonus
    //   WAREHOUSE -> 8 cột: ...|warehouseZone|overtimeHours
    //   SALES     -> 8 cột: ...|commissionRate|monthlyRevenue
    private void loadFromFile() {
        employees.clear();
        for (String line : FileIO.readLines(FILE_PATH)) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 7) {
                System.err.println("Bỏ qua dòng thiếu cột trong employees.txt: " + line);
                continue;
            }
            try {
                // Lấy mọi điểm chung nhân viên
                String id = parts[0].trim();
                String fullName = parts[1].trim();
                String phone = parts[2].trim();
                double baseSalary = Double.parseDouble(parts[3].trim());
                String role = parts[4].trim().toUpperCase();
                int hireYear = Integer.parseInt(parts[5].trim());

                Employee e;
                if (role.equals("ADMIN")) {
                    double bonus = Double.parseDouble(parts[6].trim());
                    e = new AdminStaff(id, fullName, phone, baseSalary, hireYear, bonus);
                } else if (role.equals("WAREHOUSE") && parts.length >= 8) {
                    String Zone = parts[6].trim().toUpperCase();
                    int overtime = Integer.parseInt(parts[7].trim());
                    e = new WarehouseStaff(id, fullName, phone, baseSalary, hireYear, Zone, overtime);
                } else if (role.equals("SALES") && parts.length >= 8) {
                    double rate = Double.parseDouble(parts[6].trim());
                    double revenue = Double.parseDouble(parts[7].trim());
                    e = new SalesStaff(id, fullName, phone, baseSalary, hireYear, rate, revenue);
                } else {
                    System.err.println("Bỏ qua dòng sai role hoặc thiếu cột riêng trong employees.txt: " + line);
                    continue;
                }
                employees.add(e);
            } catch (NumberFormatException exception) {
                System.err.println("Bỏ qua dòng lỗi số trong employees.txt: " + line);
            }
        }
    }

    public boolean saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Employee e : employees) {
            lines.add(e.toFileString());
        }
        return FileIO.writeLines(FILE_PATH, lines);
    }
    // ===================== TRA CỨU =====================

    public List<Employee> getAll() {
        // Sao lưu list mới
        return new ArrayList<>(employees);
    }

    public Employee findById(String id) {
        if (id == null) return null;
        for (Employee e : employees) {
            if (e.getId().equalsIgnoreCase(id.trim())) return e;
        }
        return null;
    }
    // Tìm gần đúng theo tên hoặc mã, không phân biệt hoa thường
    public List<Employee> searchByNameOrId(String key) {
        List<Employee> result = new ArrayList<>();
        if (key == null) return result;
        String k = key.trim().toLowerCase();
        for (Employee e : employees) {
            if (e.getFullName().toLowerCase().contains(k) || 
                e.getId().trim().toLowerCase().contains(k)) {
                    result.add(e);
                }
        }
        return result;
    }

    public int countByRole(Role role) {
        int count = 0;
        for (Employee e : employees) {
            if (e.getRole() == role) count++;
        }
        return count;
    }
    // Tổng quỹ lương
    public double getTotalSalary() {
        double total = 0;
        for (Employee e : employees) {
            total += e.calculateSalary();
        }
        return total;
    }
    // ===================== THÊM / SỬA / XÓA =====================
    // Lấy số lớn nhất rồi + 1
    public String generateNextId() {
        int max = 0;
        for (Employee e : employees) {
            try {
                int num = Integer.parseInt(e.getId().substring(3)); // cắt 3 ký tự đầu EMP
                if (num > max) max = num;
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                // Mã không đúng dạng EMPxx thì bỏ qua
            }
        }
        return String.format("EMP%02d", max + 1);
    }
    public boolean add(Employee e) {
        if (e == null || findById(e.getId()) != null) return false;
        employees.add(e);
        return saveToFile();
    }

    // Đổi chức vụ, tạo object mới cùng mã r thay vào
    public boolean replace(Employee newEmployee) {
        if (newEmployee == null) return false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId().equalsIgnoreCase(newEmployee.getId())) {
                employees.set(i, newEmployee);
                return saveToFile();
            }
        }
        return false;
    }
    // Không cho xóa ADMIN cuối cùng
    public boolean delete(String id) {
        Employee e = findById(id);
        if (e == null) return false;
        if (e.getRole() == Role.ADMIN && countByRole(Role.ADMIN) <= 1) return false;
        employees.remove(e);
        return saveToFile();
    }
    
}