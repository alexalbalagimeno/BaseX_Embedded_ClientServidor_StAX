package basex;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.stream.XMLStreamException;

import org.basex.query.QueryException;

public class Gui extends JFrame implements ItemListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JComboBox<String> comboBoxNomsPaisos;
	private ArrayList<String> llistaNomsPaisos;
	private String nomPaisSelected;
	private JButton btnGenera, btnHTML;
	private Manager manager;
	private List<String> grupEtnic;
	private TextArea textArea;
	private long longFront;
	private JButton btnCliSer;
	private JButton btnEmbedded;
	static final String LOCALHOST = "localhost";
	static final String ADMIN = "admin";
	static final int PORT = 1984;
	private JButton btnStax;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui frame = new Gui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws QueryException
	 * @throws IOException
	 */
	public Gui() throws QueryException, IOException, XMLStreamException, FileNotFoundException {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 60));
		contentPane.add(panel, BorderLayout.NORTH);

		comboBoxNomsPaisos = new JComboBox<String>();
//		comboBoxNomsPaisos.setModel(
//				new DefaultComboBoxModel<String>(llistaNomsPaisos.toArray((new String[llistaNomsPaisos.size()]))));
		comboBoxNomsPaisos.setEditable(false);
		comboBoxNomsPaisos.setPreferredSize(new Dimension(250, 20));
		comboBoxNomsPaisos.setMaximumRowCount(300);
		comboBoxNomsPaisos.addItemListener(this);
		panel.add(comboBoxNomsPaisos);

		btnGenera = new JButton("Genera");
		btnGenera.setPreferredSize(new Dimension(90, 23));
		btnGenera.addActionListener(this);
		panel.add(btnGenera);

		btnHTML = new JButton("HTML");
		btnHTML.setPreferredSize(new Dimension(90, 23));
		btnHTML.addActionListener(this);

		btnEmbedded = new JButton("Embedded");
		btnEmbedded.addActionListener(this);
		panel.add(btnEmbedded);

		btnCliSer = new JButton("CliSer");
		btnCliSer.addActionListener(this);
		panel.add(btnCliSer);

		btnStax = new JButton("StAX");
		btnStax.addActionListener(this);
		panel.add(btnStax);

		panel.add(btnHTML);

		textArea = new TextArea();
		contentPane.add(textArea, BorderLayout.CENTER);

	}

	private void formaLlistaPaisos(String nomsPaisos) {
		llistaNomsPaisos = new ArrayList<String>();
		String[] nomsPaisosArray = nomsPaisos.split("\n");
		for (String nomPais : nomsPaisosArray) {
			llistaNomsPaisos.add(nomPais);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		switch (arg0.getActionCommand()) {
		case "Genera":

			try {
				longFront = manager.getLongitudFronteres(nomPaisSelected);
				grupEtnic = manager.getGrupsEtnics(nomPaisSelected);
			} catch (FileNotFoundException | XMLStreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Pais pais = new Pais();
			pais.setNom(nomPaisSelected);
			pais.setLongitudFronteres(longFront);
			pais.setGrupsEtnics(grupEtnic);
			textArea.setText(pais.creaDescripcio());

			break;
		case "HTML":
			try {
				generaHtml();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "Embedded":
			try {
				manager = new Manager();
				manager.conexio = "Embedded";
				// String amb tots noms paisos
				String nomsPaisos = manager.getNomsPaisos();

				// Forma llista de strings paisos a la variable llistaNomsPaisos
				formaLlistaPaisos(nomsPaisos);

				comboBoxNomsPaisos.setModel(new DefaultComboBoxModel<String>(
						llistaNomsPaisos.toArray((new String[llistaNomsPaisos.size()]))));
				System.out.println("Embedded");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "CliSer":
			try {
				manager = new Manager();
				manager.conexio = "ClientServidor";
				// String amb tots noms paisos
				String nomsPaisos = manager.getNomsPaisos();

				// Forma llista de strings paisos a la variable llistaNomsPaisos
				formaLlistaPaisos(nomsPaisos);
				comboBoxNomsPaisos.setModel(new DefaultComboBoxModel<String>(
						llistaNomsPaisos.toArray((new String[llistaNomsPaisos.size()]))));
				System.out.println("ClientServidor");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "StAX":
			try {
				manager = new Manager();
				manager.conexio = "StaX";
				// String amb tots noms paisos
				String nomsPaisos = manager.getNomsPaisos();

				// Forma llista de strings paisos a la variable llistaNomsPaisos
				formaLlistaPaisos(nomsPaisos);
				comboBoxNomsPaisos.setModel(new DefaultComboBoxModel<String>(
						llistaNomsPaisos.toArray((new String[llistaNomsPaisos.size()]))));
				System.out.println("StAX");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}

	}

	private void generaHtml() throws Exception {

		String respostaFinal = manager.generaStringHtml();

		creaFitxerHtml(respostaFinal);

		textArea.setText(respostaFinal);
	}

	private void creaFitxerHtml(String respostaFinal) throws IOException, Exception {
		String nomFitxer = "paisos.html";
		File file = new File(nomFitxer);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(respostaFinal);
		bw.close();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getItemSelectable() == comboBoxNomsPaisos) {
				nomPaisSelected = (String) e.getItem();
			}
		}
	}
}
