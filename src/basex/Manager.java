package basex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.item.Item;

public class Manager {

	Scanner teclat;
	public String conexio;
	private ClientSession session;
	private Context context;
	private XMLInputFactory f;
	private XMLStreamReader reader;

	public Manager() throws IOException {
	}

	private void modeEmbedded() throws BaseXException {
		this.context = new Context();
		try {
			new Open("paisos").execute(context);
		} catch (Exception e) {
			// Si no es pot, la creem
			new CreateDB("paisos", "factbook.xml").execute(context);
		}
	}

	private void modeClientServidor() throws IOException {
		this.session = new ClientSession(Gui.LOCALHOST, Gui.PORT, Gui.ADMIN, Gui.ADMIN);
	}

	private void modeStax() throws FactoryConfigurationError, XMLStreamException, FileNotFoundException {
		f = XMLInputFactory.newInstance();
		reader = f.createXMLStreamReader(new FileReader("factbook.xml"));
	}

	public String getNomsPaisos() throws IOException, XMLStreamException {

		String consultaPais = null;
		String respostaNomsPaisos = "";
		if (conexio.equals("Embedded")) {
			try {
				modeEmbedded();
			} catch (BaseXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			consultaPais = "for $pais in doc('paisos')//country " + "order by $pais/name/data()"
					+ "  return $pais/name/data()";

			respostaNomsPaisos = new XQuery(consultaPais).execute(context);

			new Close().execute(context);
			context.close();
			return respostaNomsPaisos;
		} else if (conexio.equals("ClientServidor")) {
			try {

				modeClientServidor();
			} catch (IOException e) {
				e.printStackTrace();
			}
			session.execute("CREATE DATABASE paisos /dades/dades/Alex/eclipse/m06/PT32-AlexAlbalaV2/factbook.xml");

			ClientQuery consultaPaisServ = session.query("for $pais in doc('paisos')//country "
					+ "order by $pais/name/data()" + "  return $pais/name/data()");

			respostaNomsPaisos = consultaPaisServ.execute();

			session.close();
			return respostaNomsPaisos;
		} else {

			modeStax();

			boolean esticDinsDeCountry = false;

			while (reader.hasNext()) {
				int esdeveniment = reader.next();

				switch (esdeveniment) {
				case XMLStreamReader.START_ELEMENT:
					String nomElement = reader.getLocalName();

					if (nomElement.equals("country")) {
						esticDinsDeCountry = true;
					}

					if (nomElement.equals("name")) {
						if (esticDinsDeCountry) {
							respostaNomsPaisos = respostaNomsPaisos + reader.getElementText() + "\n";
						}
						esticDinsDeCountry = false;
					}

				}
			}
			return respostaNomsPaisos;
		}
	}

	public long getLongitudFronteres(String nom) throws FileNotFoundException, XMLStreamException {
		float longitud = 0;
		if (conexio.equals("Embedded")) {
			try {
				modeEmbedded();
			} catch (BaseXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String consultaLongitudsFront = "for $pais in doc('paisos')//country[name='" + nom + "']"
					+ "  return $pais/border/@length/data()";
			// Consulta iterant
			QueryProcessor proce = new QueryProcessor(consultaLongitudsFront.replaceAll("[\r\n]+", ""), context);
			Iter iter = null;
			try {
				iter = proce.iter();
				for (Item item; (item = iter.next()) != null;) {
					longitud += Float.parseFloat((String) item.toJava());
				}
				proce.close();
				new Close().execute(context);
			} catch (QueryException | BaseXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				context.close();
			}
			return (long) longitud;
		}

		else if (conexio.equals("ClientServidor")) {
			ClientQuery consultaLongitudsFront = null;
			try {

				modeClientServidor();

				String consultaString = "for $pais in doc('paisos')//country[name='" + nom + "']"
						+ "  return $pais/border/@length/data()";
				consultaLongitudsFront = session.query(consultaString.replaceAll("[\r\n]+", ""));

				while (consultaLongitudsFront.more()) {
					longitud += Float.parseFloat(consultaLongitudsFront.next());
				}

				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return (long) longitud;
		}

		else {

			modeStax();

			boolean esticDinsDeCountry = false;
			boolean esticDinsDeBorder = false;
			boolean heTrobatNomPais = false;
			String nomTrobatPais = "";
			while (reader.hasNext()) {
				int esdeveniment = reader.next();

				switch (esdeveniment) {
				case XMLStreamReader.START_ELEMENT:
					String nomElement = reader.getLocalName();

					if (nomElement.equals("country")) {
						esticDinsDeCountry = true;
					}

					if (nomElement.equals("name") && esticDinsDeCountry) {
						nomTrobatPais = reader.getElementText();
						if (nomTrobatPais.equals(nom)) {
							heTrobatNomPais = true;
						}
					}

					if (nomElement.equals("border") && heTrobatNomPais) {
						longitud = longitud + Float.parseFloat(reader.getAttributeValue(1));
						System.out.println(longitud);
						esticDinsDeBorder = true;
					}

					if (esticDinsDeBorder && !nomElement.equals("border")) {
						heTrobatNomPais = false;
					}
				}

			}
			return (long) longitud;
		}
	}

	public List<String> getGrupsEtnics(String nom) throws FileNotFoundException, XMLStreamException {
		List<String> grupsEtnics = null;
		if (conexio.equals("Embedded")) {
			try {
				modeEmbedded();
			} catch (BaseXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String consultaGrupsEtnics = "for $pais in doc('paisos')//country[name='" + nom + "']"
					+ "  return $pais/ethnicgroups/data()";
			// Consulta iterant
			QueryProcessor proce = new QueryProcessor(consultaGrupsEtnics.replaceAll("[\r\n]+", ""), context);
			Iter iter = null;
			grupsEtnics = new ArrayList<String>();
			try {
				iter = proce.iter();
				for (Item item; (item = iter.next()) != null;) {
					grupsEtnics.add((String) item.toJava());
				}
				proce.close();
				new Close().execute(context);
			} catch (QueryException | BaseXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				context.close();
			}

			return grupsEtnics;
		} else if (conexio.equals("ClientServidor")) {
			ClientQuery consultaGrupsEtnics = null;
			try {

				modeClientServidor();

				String consultaString = "for $pais in doc('paisos')//country[name='" + nom + "']"
						+ "  return $pais/ethnicgroups/data()";
				consultaGrupsEtnics = session.query(consultaString.replaceAll("[\r\n]+", ""));

				grupsEtnics = new ArrayList<String>();

				while (consultaGrupsEtnics.more()) {
					grupsEtnics.add((String) consultaGrupsEtnics.next());
				}

				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return grupsEtnics;
		} else {

			modeStax();

			grupsEtnics = new ArrayList<String>();
			boolean esticDinsDeCountry = false;
			boolean esticDinsDeGrupsEtnics = false;
			boolean heTrobatNomPais = false;
			String nomTrobatPais = "";
			while (reader.hasNext()) {
				int esdeveniment = reader.next();

				switch (esdeveniment) {
				case XMLStreamReader.START_ELEMENT:
					String nomElement = reader.getLocalName();

					if (nomElement.equals("country")) {
						esticDinsDeCountry = true;
					}

					if (nomElement.equals("name") && esticDinsDeCountry) {
						nomTrobatPais = reader.getElementText();
						if (nomTrobatPais.equals(nom)) {
							heTrobatNomPais = true;
						}
					}

					if (nomElement.equals("ethnicgroups") && heTrobatNomPais) {
						grupsEtnics.add(reader.getElementText());
						esticDinsDeGrupsEtnics = true;
					}

					if (esticDinsDeGrupsEtnics && !nomElement.equals("ethnicgroups")) {
						heTrobatNomPais = false;
					}
				}

			}
			return grupsEtnics;
		}

	}

	public String generaStringHtml() throws IOException, XMLStreamException {
		String docType = "<!DOCTYPE HTML PUBLIC" + "\"-//W3C//DTD HTML 4.01 Transitional//EN\""
				+ "\"http://www.w3.org/TR/html4/loose.dtd\">";
		String consultaHtml = "<html>" + "<head>" + "<title>" + "Informacio de paisos" + "</title>" + "</head>"
				+ "<body>" + "<table border=\"1\">" + "<tr>" + "<th>Nom</th>" + "<th>PIB</th>" + "<th>Poblacio</th>"
				+ "<th>Num ciutats</th>" + "</tr>{for $pais in doc('paisos')//country "
				+ "return <tr>{<td>{$pais/name/data()}</td>," + "<td>{$pais/gdp_total/data()}</td>,"
				+ "<td>{$pais/population/data()}</td>," + "<td>{count($pais//city)}</td>}" + "</tr>}" + "</table>"
				+ "</body>" + "</html>";

		if (conexio.equals("Embedded")) {
			try {
				modeEmbedded();
			} catch (BaseXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Consulta iterant
			String respostaConsultaHtml = null;
			try {
				respostaConsultaHtml = new XQuery(consultaHtml).execute(context);
				new Close().execute(context);
			} catch (BaseXException e) {
				e.printStackTrace();
			}
			context.close();
			String respostaFinal = docType + "\n" + respostaConsultaHtml;

			return respostaFinal;
		} else if (conexio.equals("ClientServidor")) {

			modeClientServidor();

			String respostaConsultaHtml = session.execute("XQUERY " + consultaHtml);
			String respostaFinal = docType + "\n" + respostaConsultaHtml;

			session.close();

			return respostaFinal;
		} else {

			modeStax();

			String consultaHtmlAbans = "<html>" + "<head>" + "<title>" + "Informacio de paisos" + "</title>" + "</head>"
					+ "<body>" + "<table border=\"1\">" + "<tr>" + "<th>Nom</th>" + "<th>Poblacio</th>" + "<th>PIB</th>"
					+ "<th>Num ciutats</th>" + "</tr>";
			boolean esticDinsDeCountry = false;
			boolean heTrobatNomPais = false;
			boolean heTrobatPoblacio = false;
			boolean heTrobatPIB = false;
			String nomTrobatPais = "";
			String PIB = "";
			String poblacio = "";
			String acumula = "";
			int numCiutats = 0;
			while (reader.hasNext()) {
				int esdeveniment = reader.next();

				switch (esdeveniment) {
				case XMLStreamReader.START_ELEMENT:
					String nomElement = reader.getLocalName();

					if (nomElement.equals("country")) {
						esticDinsDeCountry = true;
					}

					if (nomElement.equals("name") && esticDinsDeCountry && !heTrobatNomPais) {
						nomTrobatPais = reader.getElementText();
						acumula = acumula + "<tr><td>" + nomTrobatPais + "</td>";
						heTrobatNomPais = true;
					}

					if (nomElement.equals("population") && esticDinsDeCountry && !heTrobatPoblacio)  {
						poblacio = reader.getElementText();
						acumula = acumula + "<td>" + poblacio + "</td>";
						heTrobatPoblacio = true;
					}
					
					if (nomElement.equals("gdp_total") && esticDinsDeCountry) {
						PIB = reader.getElementText();
						acumula = acumula + "<td>" + PIB + "</td>";
						heTrobatPIB = true;
					}
					if (nomElement.equals("city") && esticDinsDeCountry) {
						numCiutats++;
					}
				case XMLStreamReader.END_ELEMENT:
					
					nomElement = reader.getLocalName();
					if (nomElement.equals("country") && numCiutats!=0) {
						if(!heTrobatPIB)
							acumula = acumula + "<td>" + 0 + "</td>";
						acumula = acumula + "<td>" + numCiutats + "</td></tr>";
						numCiutats = 0;
						heTrobatPoblacio = false;
						heTrobatNomPais = false;
						heTrobatPIB = false;
					}
				}
			}
			acumula = acumula + "</table>" + "</body>" + "</html>";
			String respostaFinal = docType + "\n" + consultaHtmlAbans + acumula;

			System.out.println(respostaFinal);
			return respostaFinal;
		}
	}
}
