/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unibi.csra.dm.view.struct;

import de.unibi.csra.dm.exception.NotAvailableException;
import de.unibi.csra.dm.struct.GlobalConfig;
import java.util.List;
import javax.swing.JTable;

/**
 *
 * @author mpohling
 */
public class GlobalConfigOverviewPanel extends AbstractOverviewPanel<GlobalConfig> {

	/**
	 * Creates new form DeviceClassOverviewPanel
	 */
	public GlobalConfigOverviewPanel() {

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

	@Override
	protected List<GlobalConfig> getContextList() {
		return deviceManager.getGlobalConfigList();
	}

	@Override
	protected String[] getContextLables() {
		String[] contextLables = {"ID","Name", "InstanceConfigMap", "Description"};
		return contextLables;
	}

	@Override
	protected void updateContextData(final GlobalConfig context, final Object[] contextData) {
		contextData[0] = context.getId();
		contextData[1] = context.getName();
		contextData[2] = context.getInstanceConfigMap(); //TODO
		contextData[3] = context.getDescription();
	}

	@Override
	protected void remove(final GlobalConfig globalConfig) {
		deviceManager.removeGlobalConfig(globalConfig.getId());
	}

	@Override
	protected void edit(final GlobalConfig globalConfig) {
		GlobalConfigEditorFrame.edit(globalConfig);
	}

	@Override
	protected void add() {
		GlobalConfig globalConfig = new GlobalConfig();
		edit(globalConfig);
	}

	@Override
	protected GlobalConfig getSelection(final JTable contextTable) throws NotAvailableException {
		return deviceManager.getGlobalConfig((String) contextTable.getModel().getValueAt(contextTable.getSelectedRow(), 0));
	}
}
