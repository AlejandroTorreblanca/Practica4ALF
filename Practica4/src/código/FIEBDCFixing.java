package c�digo;

import java.awt.List;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FIEBDCFixing {
	private static final String NOMBRE_PROGRAMA="FIEBDCFixingAT_JG";
	private static final String registro="^([ABCDEFGKLMNOPQRTWXY])\\s*((\\|[^~\\|]*)+)\\|";
	private static final String registroV="^V\\s*((\\|[^~\\|]*)+)\\|";
	private static final String campo="\\|([^~\\|]*)";			// Se podr�a dejar solo que no haya | 
	private static final String campoTexto="[^~\\|\\\\]*";		//Se podr�a dejar solo que no haya \
	private static final String campoCabecera="(([^~\\|\\\\]*)((\\\\[^~\\|\\\\]*)*))?";
	private static final String subcampoRotulo="\\\\([^~\\|\\\\]*)";
	private static final String campoCaracteres="\\s*(850|437|ANSI)\\s*";
	private static final String campoVerFormato="(\\s*FIEBDC-3/(2016|200[247]|9[85])\\s*)(\\\\([^\\\\]*))?";
	private static final String campoFecha="\\s*(([0-2][0-9]|3[01])(0[0-9]|1[0-2])(\\d{4})|(\\d)(0[0-9]|1[0-2])(\\d{4})|([0-2][0-9]|3[01])?(0[0-9]|1[0-2])(\\d{2})|(\\d)(0[0-9]|1[0-2])(\\d{2})|(\\d)?(\\d{2})|\\d)\\s*";
	private static final String campoTipoInfo="\\s*([1-4])?\\s*";
	private static final String campoNCertificacion="\\s*(\\d?\\d?)\\s*";
	private static final String campoCodigo="\\s*([\\w��\\.\\$\\#\\%\\&]{1,20})((\\\\\\s*[\\w��\\.\\$\\#\\%\\&]{1,20})*)\\s*";
	private static final String subcampoCodigo="\\s*\\\\\\s*([\\w��\\.\\$\\#\\%\\&]{1,20})\\s*";
	private static final String campoCodigoT="\\s*([\\w��\\.\\$\\#\\%\\&]{1,20})\\s*";
	private static final String campoURL="\\s*(?i)https?://([\\w-]+\\.)+[a-z]{2,6}(:\\d{1,4})?(/[\\w\\/#~:.?+=&%@~-]+)?\\s*";
	private static final String campoUnidad="\\s*(([cdk]?m[23��]?|kgr?|[cm]?[Uu][dD]|ha?|[tlda])?)\\s*";
	private static final String campoPrecio="(\\s*\\d+([.,]\\d*)?\\s*)?(\\\\\\s*\\d+([.,]\\d*)?\\s*)*\\\\?";
	private static final String subcampoPrecio="\\s*\\\\\\d+([.,]\\d*)?\\s*";
	private static final String campoTipoC="\\s*([0-5])?\\s*";
	private static StringBuffer salida;
	private static int numRotulos=0;
	private static int tipoInfo=0;
	private static boolean resumenLargo=false;
	private static String resumen="";
	private static LinkedList<String> codigos= new LinkedList<>();
	
	/**
	 * Comprueba que la extensi�n del nombre del fichero sea la correcta.
	 * @param nombreFichero nombre que se desea comprobar.
	 * @return true si tiene extensi�n .stl, false en caso contrario.
	 */
	public static boolean comprobarNombreFichero(String nombreFichero) 
	{
       Pattern pat = Pattern.compile("\\.(BC|bc)3$");
       Matcher mat = pat.matcher(nombreFichero);
       if (mat.find()) //En este caso si que se puede usar find
    	   return true; 	//Caso en el que el fichero tiene extensi�n correcta
        else 
    	   return false;    //Caso en el que el fichero no tiene extensi�n correcta
	}
	
	public static String nuevoNombreFichero(String nombreFichero){
		Pattern pat = Pattern.compile("(.*)(\\.(BC|bc)3)$");
	    Matcher mat = pat.matcher(nombreFichero);
	    if(mat.matches())
	    	return mat.group(1)+"FIXED"+mat.group(2);
	    return"";
	}
	
	public static void campoTexto(String campo){
		Pattern pat = Pattern.compile(campoTexto);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campotexto "+campo);
	    salida.append(campo);
	}
	
	public static void campoV2(String campo){
		Pattern pat = Pattern.compile(campoVerFormato);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campov2 "+campo);
	    salida.append(mat.group(1));
	    if(mat.group(4)!=null){
			salida.append("\\");
		    //
			Pattern pat3= Pattern.compile(campoFecha);
		    Matcher mat3=pat3.matcher(mat.group(4));
		    if(!mat3.matches())
		    	errorDeLectura1("campoFecha "+mat.group(4));
		    //TODO Comprobar con date
		    Date fechaActual = new Date();
		    DateFormat formatoFecha = new SimpleDateFormat("ddMMyyyy");
		    salida.append(formatoFecha.format(fechaActual));
		    //
	    }	    
	}
	
	public static void campoV3(String campo){
		Pattern pat = Pattern.compile(campoTexto);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campotexto "+campo);
	    salida.append(NOMBRE_PROGRAMA);	    
	}
	
	public static void campoFecha(String fecha){
		Pattern pat= Pattern.compile(campoFecha);
	    Matcher mat=pat.matcher(fecha);
	    if(!mat.matches())
	    	errorDeLectura1("campoFecha "+fecha);
	    //TODO Comprobar con date
	    salida.append(fecha);
	}
	public static void campoV4(String campo){
		Pattern pat = Pattern.compile(campoCabecera);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campoV4 "+campo);
	    if(mat.group(1)!=null)
	    	salida.append(mat.group(2));
	    if(mat.group(3)!=null)	
	    {
	    	Pattern pat2=Pattern.compile(subcampoRotulo);
	    	Matcher mat2=pat2.matcher(mat.group(3));
	    	while (mat2.find())
	    	{
	    		numRotulos++;
	    		salida.append(mat2.group());
	    	}
	    }
	}
	
	public static void campoV5(String campo){
		Pattern pat= Pattern.compile(campoCaracteres);
	    Matcher mat=pat.matcher(campo);
	    if(!mat.matches())
	    	errorDeLectura1("campoCaracteres "+campo);
	    salida.append("ANSI");
	}
	
	public static void campoV7(String campo){
		Pattern pat= Pattern.compile(campoTipoInfo);
	    Matcher mat=pat.matcher(campo);
	    if(!mat.matches())
	    	errorDeLectura1("campoTipoInfo "+campo);
	    salida.append(campo);
	    if(mat.group(1)!=null)
	    {
	    	System.out.println("Tipo de informaci�n:");
	    	tipoInfo=Integer.parseInt(mat.group(1));
		    switch(tipoInfo)
		    {
		    case 1:
		    	System.out.println("Base de datos.");
		    	break;
		    case 2:
		    	System.out.println("Presupuesto.");
		    	break;
		    case 3:
		    	System.out.println("Certificaci�n (a origen).");
		    	break;
		    case 4:
		    	System.out.println("Actualizaci�n de base de datos.");
		    	break;
		    default:
		    	System.err.println("Imposible");
		    	System.exit(0);
		    }
	    }
	}
	
	public static void campoV8(String campo){
		if(tipoInfo==3){
			Pattern pat= Pattern.compile(campoNCertificacion);
		    Matcher mat=pat.matcher(campo);
		    if(!mat.matches())
		    	errorDeLectura1("campoNCertificacion "+campo);
		    salida.append(campo);
		    if(mat.group(1)!=null)
		    	System.out.println("N�mero de certificaci�n: "+mat.group(1));
		}
	}
	
	public static void campoV9(String campo){
		if(tipoInfo==3){
			String campo9="("+campoFecha+")?";
			Pattern pat= Pattern.compile(campo9);
		    Matcher mat=pat.matcher(campo);
		    if(!mat.matches())
		    	errorDeLectura1("campo9 "+campo);
		    if(mat.group(1)!=null)
		    {
		    	campoFecha(mat.group(1));
		    	System.out.println("Fecha de certificaci�n: "+mat.group(1));
		    }	
		}
	}
	
	public static void campoV10(String campo){
		Pattern pat= Pattern.compile(campoURL);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("URL err�nea: "+campo);
		salida.append(campo);
	}
	
	public static void procesarRegistroV(String linea) {
		Pattern pat = Pattern.compile(registroV);
	    Matcher mat = pat.matcher(linea);
	    if (!mat.find())
	    	errorDeLectura1("Registro V ~"+linea);
	    salida.append("~V");
	    String campos=mat.group(1);
	    Pattern pat2=Pattern.compile(campo);
	    Matcher mat2=pat2.matcher(campos);
	    int i=1;
	    while(mat2.find()){
	    	salida.append("|");
	    	switch(i){
	    	case 1:
	    		campoTexto(mat2.group(1));
	    		break;
	    	case 2:
	    		campoV2(mat2.group(1));
	    		break;
	    	case 3:
	    		campoV3(mat2.group(1));
	    		break;
	    	case 4:
	    		campoV4(mat2.group(1));
	    		break;
	    	case 5:
	    		campoV5(mat2.group(1));
	    		break;
	    	case 6:
	    		campoTexto(mat2.group(1));
	    		break;
	    	case 7:
	    		campoV7(mat2.group(1));
	    		break;
	    	case 8:
	    		campoV8(mat2.group(1));
	    		break;
	    	case 9:
	    		campoV9(mat2.group(1));
	    		break;
	    	case 10:
	    		campoV10(mat2.group(1));
	    		break;
	    	default:
	    		errorDeLectura1("Registro V con demasiados campos.");
	    	}
	    	i++;
	    }
	    salida.append("|\r\n");
	}
	
	public static String campoC1(String campo){
		Pattern pat= Pattern.compile(campoCodigo);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("campoCodigo: "+campo);
		salida.append(campo);
		if(mat.group(2)!=null)
		{
			Pattern pat2= Pattern.compile(subcampoCodigo);
			Matcher mat2=pat2.matcher(mat.group(2));
			while(mat2.find())
				codigos.add(mat2.group(1));
		}
		codigos.add(mat.group(1));
		return(mat.group(1));
	}
	
	public static void campoC2(String campo){
		Pattern pat= Pattern.compile(campoUnidad);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("campoUnidad: "+campo);
		salida.append(campo);
	}
	
	public static void campoC3(String campo){
		Pattern pat= Pattern.compile(campoTexto);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("campoResumen: "+campo);
		if(campo.length()<=64)
			salida.append(campo);
		else
		{
			System.out.println("Resumen reubicado");
			resumenLargo=true;
			resumen=campo;
			salida.append("Resumen reubicado en registro TEXTO");
		}
	}
	
	public static void campoC4(String campo){
		Pattern pat= Pattern.compile(campoPrecio);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
			errorDeLectura1("Campo precio: "+campo);
		int i=0;
		if(mat.group(1)!=null){
			salida.append(mat.group(1));
			i=1;
		}			
		Pattern pat2= Pattern.compile(subcampoPrecio);
		Matcher mat2=pat2.matcher(campo);
		String texto="";
		while(i<numRotulos && mat2.find())
		{
			texto+=mat2.group();
			i++;
		}
		while(i<numRotulos)
		{
			if(i==0)
				texto+="1.0";
			else
				texto+="\\1.0";
			i++;
		}
		salida.append(texto);
	}
	
	public static void campoC5(String campo){
		Pattern pat = Pattern.compile("\\s*");
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
		    campoFecha(campo);	    
	}
	
	public static void campoC6(String campo){
		Pattern pat= Pattern.compile(campoTipoC);
	    Matcher mat=pat.matcher(campo);
	    if(!mat.matches())
	    	errorDeLectura1("campoTipoInfo "+campo);
	    salida.append(campo);
	    if(mat.group(1)!=null)
	    {
	    	System.out.println("Tipo de registro C:");
		    switch(Integer.parseInt(mat.group(1)))
		    {
		    case 0:
		    	System.out.println("Sin clasificar.");
		    	break;
		    case 1:
		    	System.out.println("Mano de obra.");
		    	break;
		    case 2:
		    	System.out.println("Maquinaria y medios auxiliares.");
		    	break;
		    case 3:
		    	System.out.println("Materiales.");
		    	break;
		    case 4:
		    	System.out.println("Componentes adicionales de residuo.");
		    	break;
		    case 5:
		    	System.out.println("Clasificaci�n de residuo.");
		    	break;
		    default:
		    	System.err.println("Imposible");
		    	System.exit(0);
		    }
	    }
	}
	
	public static void crearTipoT(String codigo){
		String texto="~T|"+codigo+"|"+resumen+"|\r\n";
		salida.append(texto);
	}
	
	public static void procesarRegistroC(String campos){
	    salida.append("~C");
	    resumenLargo=false;
	    Pattern pat2=Pattern.compile(campo);
	    Matcher mat2=pat2.matcher(campos);
	    int i=1;
	    String codigo="";
	    while(mat2.find()){
	    	salida.append("|");
	    	switch(i){
	    	case 1:
	    		codigo=campoC1(mat2.group(1));
	    		
	    		break;
	    	case 2:
	    		campoC2(mat2.group(1));
	    		break;
	    	case 3:
	    		campoC3(mat2.group(1));
	    		break;
	    	case 4:
	    		campoC4(mat2.group(1));
	    		break;
	    	case 5:
	    		campoC5(mat2.group(1));
	    		break;
	    	case 6:
	    		campoC6(mat2.group(1));
	    		break;
	    	default:
	    		errorDeLectura1("Registro C con demasiados campos.");
	    	}
	    	i++;
	    }
	    salida.append("\r\n");	    
	    if (resumenLargo)
	    	crearTipoT(codigo);
	}
	
	public static void campoT1(String campo){
		Pattern pat = Pattern.compile(campoCodigoT);
	    Matcher mat = pat.matcher(campo);
		if(!codigos.contains(campo) || !mat.matches())  
			errorDeLectura1("C�digo del registro T incorrecto. "+campo);
		salida.append(campo);
	}
	
	public static void procesarRegistroT(String campos){
	    salida.append("~T");
	    Pattern pat2=Pattern.compile(campo);
	    Matcher mat2=pat2.matcher(campos);
	    int i=1;
	    while(mat2.find()){
	    	salida.append("|");
	    	switch(i){
	    	case 1:
	    	campoT1(mat2.group(1));
	    		break;
	    	case 2:
	    		campoTexto(mat2.group(1));
	    		break;
	    	default:
	    		errorDeLectura1("Registro T con demasiados campos.");
	    	}
	    	i++;
	    }
	    salida.append("\r\n");
	}
	
	public static void procesarRegistro(String linea) {
		//XXX Que hacer si un reg K esta mal internametent.
		Pattern pat = Pattern.compile(registro);
	    Matcher mat = pat.matcher(linea);
	    if (mat.find())
	    {
	    	String cabecera=mat.group(1);
		    String cadena=mat.group(2);
		    switch (cabecera) {
			case "C":
				procesarRegistroC(cadena);
				break;
			case "T":
				procesarRegistroT(cadena);	
				break;
			default:
				salida.append("~"+mat.group()+"\r\n");
				break;
		    }
	    }
	}
	
	public static void errorDeLectura1(String msg)
	{
		System.err.println("Error en la lectura del fichero: "+msg+"\nPrograma finalizado.");
		System.exit(0);
	}

	
	public static void leerFichero(String nombreFichero)throws IOException 
	{
		File ficheroLectura = new File (nombreFichero); // Crea un objeto File a partir del nombre del fichero a leer
		Scanner scanner = new Scanner(ficheroLectura);
		Pattern pat=Pattern.compile("\\s*");
		Scanner sc = scanner.useDelimiter("~");
		if(sc.hasNext())
		{
			String linea=sc.next();
			Matcher mat=pat.matcher(linea);
			if (mat.matches())
				linea=sc.next();
			procesarRegistroV(linea);
		}
		while(sc.hasNext())
		{
			String registro=sc.next();
			procesarRegistro(registro);
		}
		sc.close();
		scanner.close();
	}
	
	public static void main(String[] args) {
		Scanner consola = new Scanner(System.in); //Crea Scanner para leer por consola
		salida=new StringBuffer();
		boolean control=true;
		String nombreFichero="p.bc3";
		while(control)
		{
			/*System.out.println("Nombre del fichero a leer con extensi�n .bc3 o .BC3: ");
			nombreFichero=consola.nextLine(); //lee la l�nea con el nombre de fichero  desde consola
		    if(!comprobarNombreFichero(nombreFichero))
				System.err.println("Extensi�n del fichero incorrecta, asegurese de que tiene extensi�n .bc3 o .BC3.");
			else 
			{*/
				try {
					leerFichero(nombreFichero);	//se intenta leer el contenido de fichero
					control=false;			  
				} catch (IOException e1) {
					System.err.println("***Error de lectura***\n Asegurese de que el fichero existe."); 
					control=true;
				    } 	
			//}
		}
		File fichero = new File (nuevoNombreFichero(nombreFichero)); // Crea un objeto File a partir del nombre de fichero a guardar
	    PrintWriter escritor;
		try {
			escritor = new PrintWriter(fichero);
		    escritor.print(salida); //  escribe el  texto en fichero
		    escritor.close(); //cierro el  escritor creado.		
		} catch (FileNotFoundException e) {
		}			
		consola.close();
	}

}
