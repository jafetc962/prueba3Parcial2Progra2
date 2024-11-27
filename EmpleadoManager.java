/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author HP
 */
public class EmpleadoManager {

    private RandomAccessFile rcods, remps;

    public EmpleadoManager() {
        try {
            File mf = new File("company");
            mf.mkdir();

            rcods = new RandomAccessFile("company/codigos.emp", "rw");
            remps = new RandomAccessFile("company/empleados.emp", "rw");

            initCodes();
        } catch (IOException e) {

        }
    }

    private void initCodes() throws IOException {
        if (rcods.length() == 0) {
            //Puntero rcods.writeInt(v: 1) //Puntero -> 
            rcods.writeInt(1);
        }
    }

    private int getCode() throws IOException {
        rcods.seek(0);

        int code = rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code + 1);
        return code;
    }

    public void addEmployee(String name, double salary) throws IOException {
        //asegurar el puntero este al final del archivo (PRIMER REGISTRO)
        remps.seek(remps.length());
        int code = getCode();
        //codigo P=0
        remps.writeInt(code);
        //nombre P=4
        remps.writeUTF(name);
        //salario P=12( SI EL NOMBRE ES ANA EJEMPLO )   ---CANTIDAD CARACTERES*2 +2---
        remps.writeDouble(salary);
        //fecha contratacion P=20
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        //fecha despido--nula P=28
        remps.writeLong(0);
        //BITES TOTALES P=36

        //Asegurar crear folders y archivos individuales
        createEmployeeFolder(code);
    }

    private String employeeFolder(int code) {
        return "company/empleado" + code;
    }

    private void createEmployeeFolder(int code) throws IOException {
        //Crear folder employe + code
        File edir = new File(employeeFolder(code));
        edir.mkdir();
        //crear el archivo de ventas
        createYearSalesFileFor(code);
    }

    private RandomAccessFile salesFileFor(int code) throws IOException {
        String dirPadre = employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = dirPadre + "/ventas" + yearActual + ".emp";
        return new RandomAccessFile(path, "rw");

    }

    private void createYearSalesFileFor(int code) throws IOException {

        RandomAccessFile ryear = salesFileFor(code);

        if (ryear.length() == 0) {
            for (int mes = 0; mes < 12; mes++) {
                ryear.writeDouble(0);
                ryear.writeBoolean(true);

            }
        }
    }

    public void employeeList() throws IOException {

        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int code = remps.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            Date dateH = new Date(remps.readLong());
            if (remps.readLong() == 0) {
                System.out.println("Codigo: " + code + " -Empleado: " + name + " -Salario: " + salary
                        + " -Contratado: " + dateH);
            }
        }
    }

    private boolean isEmployeeActive(int code) throws IOException { //Funcion que me deja el puntero despues del codigo de los empleados activos
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int codigo = remps.readInt();
            long pos = remps.getFilePointer(); // guarda la poscicion donde esta el codigo (Para leer todo lo que va despues del codigo)
            remps.readUTF();
            remps.skipBytes(16); //nos saltamos 16 bytes, lo cual equivale al double salay y long contratacion

            if (remps.readLong() == 0 && codigo == code) {
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }

    public boolean fireEmployee(int code) throws IOException {
        if (isEmployeeActive(code)) { // el puntero ya queda despues del codigo con el empleado activo
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiento a: " + name);
            return true;
        }
        return false;
    }

    public void addSaleToEmployee(int code, double monto) throws IOException {
        if (isEmployeeActive(code)) {
            RandomAccessFile salesFile = salesFileFor(code);

            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            salesFile.seek(currentMonth * 9); // Cada mes ocupa 9 bytes (8 para double + 1 para boolean)

            double currentSales = salesFile.readDouble();
            salesFile.seek(currentMonth * 9); // Volvemos al inicio del registro del mes
            salesFile.writeDouble(currentSales + monto); // Sumamos las ventas
            System.out.println("Venta de " + monto + " agregada al empleado " + code);
        } else {
            System.out.println("El empleado con código " + code + " no está activo.");
        }
    }

    public void payEmployee(int code) throws IOException {
        if (isEmployeeActive(code)) {
            RandomAccessFile salesFile = salesFileFor(code);

            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            salesFile.seek(currentMonth * 9 + 8); // Posición del booleano "pagado"
            if (!salesFile.readBoolean()) {
                System.out.println("El empleado ya fue pagado este mes.");
                return;
            }

            remps.seek(0);
            while (remps.getFilePointer() < remps.length()) {
                int empCode = remps.readInt();
                String name = remps.readUTF();
                double baseSalary = remps.readDouble();
                remps.skipBytes(16);

                if (empCode == code) {
                    salesFile.seek(currentMonth * 9); // Posición de ventas del mes
                    double totalSales = salesFile.readDouble();
                    double commission = totalSales * 0.10;
                    double grossSalary = baseSalary + commission;
                    double deduction = grossSalary * 0.035;
                    double netSalary = grossSalary - deduction;

                    String receiptPath = employeeFolder(code) + "/recibos.emp";
                    RandomAccessFile receiptFile = new RandomAccessFile(receiptPath, "rw");
                    receiptFile.seek(receiptFile.length()); // Ir al final del archivo

                    receiptFile.writeLong(new Date().getTime());
                    receiptFile.writeDouble(commission);
                    receiptFile.writeDouble(grossSalary);
                    receiptFile.writeDouble(deduction);
                    receiptFile.writeDouble(netSalary);
                    receiptFile.writeInt(Calendar.getInstance().get(Calendar.YEAR));
                    receiptFile.writeInt(currentMonth);

                    salesFile.seek(currentMonth * 9 + 8); // Marcar como pagado
                    salesFile.writeBoolean(false);

                    System.out.println("Empleado: " + name + " ha sido pagado este mes. Sueldo neto: " + netSalary);
                    return;
                }
            }
        } else {
            System.out.println("El empleado no está activo o no existe.");
        }
    }

    public void printEmployee(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int empCode = remps.readInt();
            String name = remps.readUTF();
            double baseSalary = remps.readDouble();
            Date hireDate = new Date(remps.readLong());
            long fireDate = remps.readLong();

            if (empCode == code) {
                System.out.println("Código: " + empCode);
                System.out.println("Nombre: " + name);
                System.out.println("Salario base: " + baseSalary);
                System.out.println("Contratado: " + hireDate);
                System.out.println(fireDate == 0 ? "Activo" : "Despedido: " + new Date(fireDate));

                RandomAccessFile salesFile = salesFileFor(code);
                double totalSales = 0;

                System.out.println("Ventas del año actual:");
                for (int month = 0; month < 12; month++) {
                    salesFile.seek(month * 9);
                    double sales = salesFile.readDouble();
                    totalSales += sales;
                    System.out.println("Mes " + (month + 1) + ": " + sales);
                }
                System.out.println("Total de ventas del año: " + totalSales);

                String receiptPath = employeeFolder(code) + "/recibos.emp";
                RandomAccessFile receiptFile = new RandomAccessFile(receiptPath, "rw");
                int receiptCount = 0;

                System.out.println("Recibos históricos:");
                while (receiptFile.getFilePointer() < receiptFile.length()) {
                    long paymentDate = receiptFile.readLong();
                    double commission = receiptFile.readDouble();
                    double grossSalary = receiptFile.readDouble();
                    double deduction = receiptFile.readDouble();
                    double netSalary = receiptFile.readDouble();
                    int year = receiptFile.readInt();
                    int month = receiptFile.readInt();

                    System.out.println("Recibo #" + (++receiptCount));
                    System.out.println("Fecha: " + new Date(paymentDate));
                    System.out.println("Comisión: " + commission);
                    System.out.println("Sueldo base: " + grossSalary);
                    System.out.println("Deducción: " + deduction);
                    System.out.println("Sueldo neto: " + netSalary);
                    System.out.println("Año: " + year + " Mes: " + (month + 1));
                    System.out.println("---------------");
                }
                return;
            }
        }
        System.out.println("Empleado con código " + code + " no encontrado.");
    }

}
