package org.sparqlscape;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class NameSpaceEditor {

	public static Map<String,String> EditDialog( Map<String,String> props ) {
		final JDialog frame = new JDialog();
		final Map<String,String> out = new HashMap<String,String>(props);
		JPanel panel = new JPanel();
		panel.setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		//Edge List
		final DefaultTableModel tableModel = new DefaultTableModel(props.size(), 2);		
		int i = 0;
		for ( String key : props.keySet() ) {
			tableModel.setValueAt(key, i, 0);
			tableModel.setValueAt(props.get(key) , i, 1);
			System.out.println( key + " " + props.get(key) );
			i++;
		}
		Object cols[] = {"Namespace", "Base URL"};
		tableModel.setColumnIdentifiers( cols );
		final JTable table = new JTable( tableModel );
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(500);

		//edgeTypeList.setVisibleRowCount(20);
		JScrollPane propScrollPane = new JScrollPane(table);
		propScrollPane.setPreferredSize(new Dimension(600, 200) );
		c.gridx=0;
		panel.add( propScrollPane, c );		
		
		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 1;
		Button delButton = new Button("Delete Row");
		delButton.addActionListener( new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.removeRow( table.getSelectedRow() );
			}
		});
		panel.add( delButton, c);
		c.gridx = 1;
		Button addButton = new Button("Add Row");
		addButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object row[] = {"",""};
				tableModel.addRow( row );
			}
		});
		panel.add( addButton, c );

		c.gridx = 0;
		c.gridy = 2;
		Button okButton = new Button("OK");
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				out.clear();
				for ( int i = 0; i < tableModel.getRowCount(); i++ ) {
					out.put( (String)tableModel.getValueAt(i, 0) , (String)tableModel.getValueAt(i, 1));
				}
				frame.setVisible(false);
			}
		});
		panel.add( okButton, c );
		
		frame.add( panel );
		frame.setLocationRelativeTo(null);
		frame.setSize(600,350);
		frame.setModal(true);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		frame.setVisible(true);
		return out;
	}	
	
	public static void main(String []args) {
		PreferenceInterface prefs = new PreferenceInterface();
		System.out.println( NameSpaceEditor.EditDialog( prefs.getNameSpaces()  ) );
	}	
}
