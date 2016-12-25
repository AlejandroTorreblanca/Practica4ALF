package código;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FIEBDCFixing {
	private static final String NOMBRE_PROGRAMA="FIEBDCFixingAT_JG";
	private static final String formatoGeneral="^([A-Z])\\s*((\\|[^~\\|]*)+)\\|\\s*";
	private static final String registro="^([ABCDEFGKLMNOPQRTWXY])\\s*((\\|[^~\\|]*)+)\\|\\s*";
	private static final String registroV="^V\\s*((\\|[^~\\|]*)+)\\|\\s*";
	private static final String campo="\\|([^~\\|]*)";			 
	private static final String campoTexto="[^~\\|\\\\]*";		
	private static final String campoCabecera="(([^~\\|\\\\]*)((\\\\[^~\\|\\\\]*)*))?";
	private static final String subcampoRotulo="\\\\([^~\\|\\\\]*)";
	private static final String campoCaracteres="(850|437|ANSI)?\\s*";
	private static final String campoVerFormato="(FIEBDC-3/(2016|200[247]|9[85])\\s*)(\\\\([^\\\\]*))?";
	private static final String campoFecha="(([0-2][0-9]|3[01])(0[0-9]|1[0-2])(\\d{4})|(\\d)(0[0-9]|1[0-2])(\\d{4})|([0-2][0-9]|3[01])?(0[0-9]|1[0-2])(\\d{2})|(\\d)(0[0-9]|1[0-2])(\\d{2})|(\\d)?(\\d{2})|\\d)\\s*";
	private static final String campoTipoInfo="([1-4])?\\s*";
	private static final String campoNCertificacion="(\\d?\\d?)\\s*";
	private static final String campoCodigo="([\\wñÑ\\.\\$\\#\\%\\&]{1,20})((\\s*\\\\[\\wñÑ\\.\\$\\#\\%\\&]{1,20})*)\\s*";
	private static final String subcampoCodigo="\\s*\\\\([\\wñÑ\\.\\$\\#\\%\\&]{1,20})\\s*";
	private static final String campoCodigoT="([\\wñÑ\\.\\$\\#\\%\\&]{1,20})\\s*";
	private static final String campoURL="((?i)https?://([\\w-]+\\.)+[a-z]{2,6}(:\\d{1,4})?(/[\\w\\/#~:.?+=&%@~-]+)?\\s*)?";
	private static final String campoUnidad="(([cdk]?m[23²³]?|[dcm]?l|%|kgr?|[cm]?[Uu][dD]?|ha?|[tlda])?)\\s*";
	private static final String campoPrecio="(\\d+([.,]\\d*)?\\s*)?(\\s*\\\\\\d+([.,]\\d*)?\\s*)*\\\\?";
	private static final String subcampoPrecio="\\s*\\\\\\d+([.,]\\d*)?\\s*";
	private static final String campoTipoC="([0-5])?\\s*";
	private static final String barrasFinales="(\\|\\s*){2,}\\s*$";
	private static StringBuffer salida;
	private static int numRotulos=0;
	private static int tipoInfo=0;
	private static boolean resumenLargo=false;
	private static String resumen="";
	private static LinkedList<String> codigos= new LinkedList<>();
	
	/**
	 * Comprueba que la extensión del nombre del fichero sea la correcta.
	 * @param nombreFichero nombre que se desea comprobar.
	 * @return true si tiene extensión .stl, false en caso contrario.
	 */
	public static boolean comprobarNombreFichero(String nombreFichero) 
	{
       Pattern pat = Pattern.compile("\\.(BC|bc)3$");
       Matcher mat = pat.matcher(nombreFichero);
       if (mat.find()) //En este caso si que se puede usar find
    	   return true; 	//Caso en el que el fichero tiene extensión correcta
        else 
    	   return false;    //Caso en el que el fichero no tiene extensión correcta
	}
	
	/**
	 * Crea un string con el sufijo FIXED.
	 * @param nombreFichero al que se le quiere añadir el sufijo.
	 * @return String con el sufijo ya concatenado.
	 */
	public static String nuevoNombreFichero(String nombreFichero){
		Pattern pat = Pattern.compile("(.*)(\\.(BC|bc)3)$");
	    Matcher mat = pat.matcher(nombreFichero);
	    if(mat.matches())
	    	return mat.group(1)+"FIXED"+mat.group(2);
	    return"";
	}
	
	/**
	 * Pasamos el formato del año de dos dígitos a cuatro aplicando la regla 80.
	 * @param year Año en formato de dos dígitos.
	 * @return Año en formato de cuatro dígitos.
	 */
	public static String regla80(String year){
		int y=Integer.parseInt(year);
		if (y>=80)
			return "19"+year;
		else
			return "20"+year;
	}
	
	/**
	 * Tratamiento de fechas para que se ajusten al formato solicitado.
	 * @param fecha en cualquier tipo de formaro.
	 * @return fecha en formato DDMMAAAA.
	 */
	public static String tratarFecha(Matcher fecha)
	{
		String fixed="";
		int y=0,m=0,d=0;
		boolean date=false;
		switch (fecha.group(1).length()) {
		case 1:
			fixed+="0000200"+fecha.group(1);
			break;
		case 2:
			fixed+="0000"+regla80(fecha.group(15));
			break;
		case 3:
			fixed+="000"+fecha.group(14)+regla80(fecha.group(15));
			break;
		case 4:
			fixed+="00"+fecha.group(9)+regla80(fecha.group(10));
			break;
		case 5:
			fixed+="0"+fecha.group(11)+fecha.group(12)+regla80(fecha.group(13));
			if(!(fecha.group(11).equals("0") || fecha.group(12).equals("00")))
			{
				date=true;
				d=Integer.parseInt(fecha.group(11));
				m=Integer.parseInt(fecha.group(12));
				y=Integer.parseInt(regla80(fecha.group(13)));
				
			}
			break;
		case 6:
			fixed+=fecha.group(8)+fecha.group(9)+regla80(fecha.group(10));
			if(!(fecha.group(8).equals("00") || fecha.group(9).equals("00")))
			{
				date=true;
				d=Integer.parseInt(fecha.group(8));
				m=Integer.parseInt(fecha.group(9));
				y=Integer.parseInt(regla80(fecha.group(10)));
				
			}
			break;
		case 7:
			fixed+="0"+fecha.group(1);
			if(!(fecha.group(5).equals("0") || fecha.group(6).equals("00") || fecha.group(7).equals("0000")))
			{
				date=true;
				d=Integer.parseInt(fecha.group(5));
				m=Integer.parseInt(fecha.group(6));
				y=Integer.parseInt(fecha.group(7));
				
			}
			break;
		case 8:
			fixed=fecha.group(1);
			if(!(fecha.group(2).equals("00") || fecha.group(3).equals("00") || fecha.group(4).equals("0000")))
			{
				date=true;
				d=Integer.parseInt(fecha.group(2));
				m=Integer.parseInt(fecha.group(3));
				y=Integer.parseInt(fecha.group(4));
				
			}
			break;
		default:
			errorDeLectura1("Fecha panic");
			break;
		}
		if(date)
			LocalDate.of(y, m, d);
		return fixed;
	}
	
	/**
	 * Tratamiento del campo texto.
	 * @param campo texto que se desea tratar
	 */
	public static void campoTexto(String campo){
		Pattern pat = Pattern.compile(campoTexto);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campotexto "+campo);
	    salida.append(campo);
	}
	
	/**
	 * Tratamiento del campo Version formato del registro V.
	 * @param campo que se desea tratar.
	 */
	public static void campoV2(String campo){
		Pattern pat = Pattern.compile(campoVerFormato);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campov2 "+campo);
	    salida.append(mat.group(1));
	    System.out.println(mat.group(1)+". ");
	    if(mat.group(4)!=null){
			Pattern pat3= Pattern.compile(campoFecha);
		    Matcher mat3=pat3.matcher(mat.group(4));
		    if(!mat3.matches())
		    	errorDeLectura1("campoFecha "+mat.group(4));
		    tratarFecha(mat3);
	    }	
		salida.append("\\");
	    Date fechaActual = new Date();
	    DateFormat formatoFecha = new SimpleDateFormat("ddMMyyyy");
	    salida.append(formatoFecha.format(fechaActual));
	}
	
	/**
	 * Tratamiento del campo Programa emisión del registro V.
	 * @param campo que se desea tratar.
	 */
	public static void campoV3(String campo){
		Pattern pat = Pattern.compile(campoTexto);
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
	    	errorDeLectura1("campotexto "+campo);
	    salida.append(NOMBRE_PROGRAMA);	    
	}
	
	/**
	 * Tratamiento del campo fecha.
	 * @param campo que se desea tratar.
	 */
	public static void campoFecha(String fecha){
		Pattern pat= Pattern.compile(campoFecha);
	    Matcher mat=pat.matcher(fecha);
	    if(!mat.matches())
	    	errorDeLectura1("campoFecha "+fecha);
	    salida.append(tratarFecha(mat));
	}
	
	/**
	 * Tratamiento del campo Cabecera del registro V.
	 * @param campo que se desea tratar.
	 */
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
	
	/**
	 * Tratamiento del campo Juego caracteres del registro V.
	 * @param campo que se desea tratar.
	 */
	public static void campoV5(String campo){
		Pattern pat= Pattern.compile(campoCaracteres);
	    Matcher mat=pat.matcher(campo);
	    if(!mat.matches())
	    	errorDeLectura1("campoCaracteres "+campo);
	    salida.append("ANSI");
	}
	
	/**
	 * Tratamiento del campo Tipo información del registro V.
	 * @param campo que se desea tratar.
	 */
	public static void campoV7(String campo){
		Pattern pat= Pattern.compile(campoTipoInfo);
	    Matcher mat=pat.matcher(campo);
	    if(!mat.matches())
	    	errorDeLectura1("campoTipoInfo "+campo);
	    salida.append(campo);
	    if(mat.group(1)!=null)
	    {
	    	System.out.println("Tipo de información:");
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
		    	System.out.println("Certificación (a origen).");
		    	break;
		    case 4:
		    	System.out.println("Actualización de base de datos.");
		    	break;
		    default:
		    	System.err.println("Imposible");
		    	System.exit(0);
		    }
	    }
	}
	
	/**
	 * Tratamiento del campo Número certificación del registro V.
	 * @param campo que se desea tratar.
	 */
	public static void campoV8(String campo){
		if(tipoInfo==3){
			Pattern pat= Pattern.compile(campoNCertificacion);
		    Matcher mat=pat.matcher(campo);
		    if(!mat.matches())
		    	errorDeLectura1("campoNCertificacion "+campo);
		    salida.append(campo);
		    if(mat.group(1)!=null)
		    	System.out.println("Número de certificación: "+mat.group(1));
		}
	}
	
	/**
	 * Tratamiento del campo Fecha certificación del registro V.
	 * @param campo que se desea tratar.
	 */
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
		    	System.out.println("Fecha de certificación: "+mat.group(1));
		    }	
		}
	}
	
	/**
	 * Tratamiento del campo URL base del registro V.
	 * @param campo que se desea tratar.
	 */
	public static void campoV10(String campo){
		Pattern pat= Pattern.compile(campoURL);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("URL errónea: "+campo);
		salida.append(campo);
	}
	
	/**
	 * Comprobación de que el formato del registro V es el adecuado. 
	 * @param linea registro V que se desea procesar.
	 */
	public static void procesarRegistroV(String linea) {
		System.out.print("Procesando registro V: Versión del formato: ");
		Pattern pat = Pattern.compile(registroV);
	    Matcher mat = pat.matcher(linea);
	    if (!mat.matches())
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
	    while (i<6){
	    	salida.append("|");
	    	switch(i){
	    	case 3:
	    		campoV3("");
	    		break;
	    	case 5:
	    		campoV5("");
	    		break;
	    	default:
	    		;
	    	}
	    	i++;
	    }
	    salida.append("|\r\n");
	    salida=eliminarBarrasFinales(salida);
	}
	
	/**
	 * Tratamiento del campo Código del registro C.
	 * @param campo que se desea tratar.
	 */
	public static String campoC1(String campo){
		Pattern pat= Pattern.compile(campoCodigo);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("campoCodigo: "+campo);
		salida.append(campo);
		System.out.println(campo);
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
	
	/**
	 * Tratamiento del campo Unidad del registro C.
	 * @param campo que se desea tratar.
	 */
	public static void campoC2(String campo){
		Pattern pat= Pattern.compile(campoUnidad);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
		   	errorDeLectura1("campoUnidad: "+campo);
		Pattern pat1= Pattern.compile("2");
		Matcher mat1=pat1.matcher(campo);
		String corregido=mat1.replaceAll("²");
		Pattern pat2= Pattern.compile("3");
		Matcher mat2=pat2.matcher(corregido);
		corregido=mat2.replaceAll("³");
		Pattern pat3= Pattern.compile("[Uu][dD]");
		Matcher mat3=pat3.matcher(corregido);
		corregido=mat3.replaceAll("u");
		Pattern pat4= Pattern.compile("kgr");
		Matcher mat4=pat4.matcher(corregido);
		corregido=mat4.replaceAll("kg");
		salida.append(corregido);
	}
	
	/**
	 * Tratamiento del campo Resumen(64) del registro C.
	 * @param campo que se desea tratar.
	 */
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
	
	/**
	 * Tratamiento del campo Precio del registro C.
	 * @param campo que se desea tratar.
	 */
	public static void campoC4(String campo){
		String ultimoPrecio="1.0";
		Pattern pat= Pattern.compile(campoPrecio);
		Matcher mat=pat.matcher(campo);
		if(!mat.matches())
			errorDeLectura1("Campo precio: "+campo);
		int i=0;
		if(mat.group(1)!=null && i<numRotulos){
			salida.append(mat.group(1));
			ultimoPrecio=mat.group(1);
			i=1;
		}			
		Pattern pat2= Pattern.compile(subcampoPrecio);
		Matcher mat2=pat2.matcher(campo);
		String texto="";
		while(i<numRotulos && mat2.find())
		{
			texto+=mat2.group();
			ultimoPrecio=mat2.group();
			i++;
		}
		while(i<numRotulos)
		{
			if(i==0)
				texto+=ultimoPrecio;
			else
				texto+="\\"+ultimoPrecio;
			i++;
		}
		salida.append(texto);
	}
	
	/**
	 * Tratamiento del campo Fecha del registro C.
	 * @param campo que se desea tratar.
	 */
	public static void campoC5(String campo){
		Pattern pat = Pattern.compile("\\s*");
	    Matcher mat = pat.matcher(campo);
	    if (!mat.matches())
		    campoFecha(campo);	    
	}
	
	/**
	 * Tratamiento del campo Tipo del registro C.
	 * @param campo que se desea tratar.
	 */
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
		    	System.out.println("Clasificación de residuo.");
		    	break;
		    default:
		    	System.err.println("Imposible");
		    	System.exit(0);
		    }
	    }
	}
	
	/**
	 * Creamos un nuevo registro de tipo T y lo añadimos al fichero de salida.
	 * @param codigo que queremos que contenga el registro de tipo T.
	 */
	public static void crearTipoT(String codigo){
		String texto="~T|"+codigo+"|"+resumen+"|\r\n";
		salida.append(texto);
	}
	
	/**
	 * Comprobación de que el formato del registro C es el adecuado. 
	 * @param campos Registro C que se desea procesar.
	 */
	public static void procesarRegistroC(String campos){
		System.out.print("Procesando registro de tipo concepto con código: ");
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
	    salida.append("|\r\n");	
	    salida=eliminarBarrasFinales(salida);
	    if (resumenLargo)
	    	crearTipoT(codigo);
	}
	
	/**
	 * Tratamiento del campo Código concepto del registro T.
	 * @param campo que se desea tratar.
	 */
	public static void campoT1(String campo){
		Pattern pat = Pattern.compile(campoCodigoT);
	    Matcher mat = pat.matcher(campo);
		if(!codigos.contains(campo) || !mat.matches())  
			errorDeLectura1("Código del registro T incorrecto. "+campo);
		salida.append(campo);
	}
	
	/**
	 * Comprobación de que el formato del registro T es el adecuado. 
	 * @param campos Registro T que se desea procesar.
	 */
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
	    salida.append("|\r\n");
	}
	
	/**
	 * Procesamos un registro, ditinguimos de que tipo és y según esto aplicamos el tratamiento correspondiente.
	 * @param linea registro que deseamos procesar.
	 */
	public static void procesarRegistro(String linea) {
		Pattern pat2 = Pattern.compile(formatoGeneral);
	    Matcher mat2 = pat2.matcher(linea);
	    if (!mat2.matches())
	    	errorDeLectura1("Formato inválido: ~"+linea);
	    Pattern pat = Pattern.compile(registro);
	    Matcher mat = pat.matcher(linea);
	    if (mat.matches())
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
				salida.append("~"+mat.group(1)+mat.group(2)+"|\r\n");
				salida=eliminarBarrasFinales(salida);
				break;
		    }
	    }
	}
	
	/**
	 * Imprimimos por panatalla un coódigo de error.
	 * @param msg mensaje que deseamos mostrar.
	 */
	public static void errorDeLectura1(String msg)
	{
		System.err.println("Error en la lectura del fichero: "+msg+"\nPrograma finalizado.");
		System.exit(0);
	}

	/**
	 * Eliminamos las barras finales repetidas que contenga un registro y que sean inecesarias.
	 * @param linea Registro a depurar.
	 * @return registro depurado.
	 */
	public static StringBuffer eliminarBarrasFinales(StringBuffer linea)
	{
		StringBuffer buff=new StringBuffer();
		Pattern pat= Pattern.compile(barrasFinales);
		Matcher mat=pat.matcher(linea);
		String corregido=mat.replaceAll("|\r\n");
		buff.append(corregido);
		return buff;
	}
	
	/**
	 * Leemos el fichero con formato .bc3 y vamos procesando cada uno de los registros que contiene.
	 * @param nombreFichero nombre del fichero que se desea procesar.
	 * @throws IOException no se puede leer el fichero con el nombre introducido.
	 */
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
		String nombreFichero="";
		while(control)
		{
			System.out.println("Nombre del fichero a leer con extensión .bc3 o .BC3: ");
			nombreFichero=consola.nextLine(); //lee la línea con el nombre de fichero  desde consola
		    if(!comprobarNombreFichero(nombreFichero))
				System.err.println("Extensión del fichero incorrecta, asegurese de que tiene extensión .bc3 o .BC3.");
			else 
			{
				try {
					leerFichero(nombreFichero);	//se intenta leer el contenido de fichero
					control=false;			  
				} catch (IOException e1) {
					System.err.println("***Error de lectura***\n Asegurese de que el fichero existe."); 
					control=true;
				    } 	
			}
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
