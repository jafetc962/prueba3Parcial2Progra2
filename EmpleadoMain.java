/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author HP
 */
import java.io.IOException;
import java.util.Scanner;

public class EmpleadoMain {
    public static void main(String[] args) {
        Scanner lea = new Scanner(System.in);
        EmpleadoManager empleadoManager = new EmpleadoManager();
        int opcion=0;

        while (opcion!=7) {
            System.out.println("\n--- Menú Principal ---");
            System.out.println("1- Agregar Empleado");
            System.out.println("2- Listar Empleados No Despedidos");
            System.out.println("3- Agregar Venta al Empleado");
            System.out.println("4- Pagar Empleado");
            System.out.println("5- Despedir a Empleado");
            System.out.println("6- Imprimir Datos del Empleado");
            System.out.println("7- Salir");
            System.out.print("Escoja una opción: ");
            
            opcion = lea.nextInt();
            lea.nextLine(); 
            
            try {
                switch (opcion) {
                    case 1: 
                        System.out.print("Ingrese el nombre del empleado: ");
                        String nombre = lea.nextLine();
                        System.out.print("Ingrese el salario del empleado: ");
                        double salario = lea.nextDouble();
                        empleadoManager.addEmployee(nombre, salario);
                        System.out.println("Empleado agregado correctamente.");
                        break;

                    case 2:
                        System.out.println("Listado de empleados activos:");
                        empleadoManager.employeeList();
                        break;

                    case 3:
                        System.out.print("Ingrese el código del empleado: ");
                        int codigoVenta = lea.nextInt();
                        System.out.print("Ingrese el monto de la venta: ");
                        double montoVenta = lea.nextDouble();
                        empleadoManager.addSaleToEmployee(codigoVenta, montoVenta);
                        break;

                    case 4: 
                        System.out.print("Ingrese el código del empleado: ");
                        int codigoPago = lea.nextInt();
                        empleadoManager.payEmployee(codigoPago);
                        break;

                    case 5:
                        System.out.print("Ingrese el código del empleado: ");
                        int codigoDespedir = lea.nextInt();
                        if (empleadoManager.fireEmployee(codigoDespedir)) {
                            System.out.println("Empleado despedido correctamente.");
                        } else {
                            System.out.println("El empleado no está activo o no existe.");
                        }
                        break;

                    case 6:
                        System.out.print("Ingrese el código del empleado: ");
                        int codigoImprimir = lea.nextInt();
                        empleadoManager.printEmployee(codigoImprimir);
                        break;

                    case 7: 
                        System.out.println("Saliendo del sistema...");
                        return;

                    default:
                        System.out.println("Opción no válida. Intente nuevamente.");
                }
            } catch (IOException e) {
                System.out.println("Ocurrió un error: " + e.getMessage());
            }
        }
    }
}
