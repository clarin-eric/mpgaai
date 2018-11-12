package de.mpg.aai.idpproxy.connector.pool;

import java.util.Date;
import java.util.LinkedList;

import de.mpg.aai.idpproxy.connector.ConnectorException;
import de.mpg.aai.idpproxy.connector.decoder.RemoteUser;


/**
 * manages the logged-in users
 * @author thajek
 */
public class PoolDataManager {
	
	private static PoolDataManager poolDataManager;
	
	private LinkedList<PoolData> poolDataList = new LinkedList<PoolData>();
	
	/** lifttime of a day */
	private final long lifeTime = 1000*60*60*24;
	
	
	/** 
	 * HIDDEN default constructor
	 */
	private PoolDataManager() {
	}
	
	
	/**
	 * Ereugt eine Instance bzw. L�d diese
	 * @return
	 */
	public static PoolDataManager getInstance() {
		if(poolDataManager == null)
			poolDataManager = new PoolDataManager();
		return poolDataManager;
	}
	
	
	/**
	 * Sucht den ersten auftrehtenden Benutzer namen in der Liste und gibt deren daten zur�ck
	 * @param userName
	 * @throws ConnectorException 
	 * Wird geworfen wenn kein Benutzer gefunden wurde
	 */
	public RemoteUser searchPrincipal(String userName, String realm) throws ConnectorException {
		for(int x = (this.poolDataList.size()-1) ; x >= 0 ; x--) {
			PoolData poolData = this.poolDataList.get(x);
			RemoteUser remoteUser = poolData.getRemoteUser();
			if(remoteUser.getRemoteUser().equals(userName) && remoteUser.getRealm().equals(realm))
				return remoteUser;
		}
		throw new ConnectorException("Benutzername nicht in der Liste");
	}
	
	
	/**
	 * F�gt einen RemoteUser zu der Liste hinzu und l�scht fall schon 
	 * ein eintrag vorhanden ist diesen
	 * @param remoteUser
	 */
	public void addRemoteUser(RemoteUser remoteUser) {
		removeOldEntrys();
		PoolData poolData = new PoolData(remoteUser);
		this.poolDataList.addLast(poolData);
	}
	
	
	/**
	 * Entfernd alte eintr�ge ab einer bestimmten lebensdauer
	 */
	private void removeOldEntrys() {
		Date date = new Date();
		long diff = 0;
		while(!this.poolDataList.isEmpty()) {
			PoolData poolData = this.poolDataList.getFirst();
			//Zeit an dem die PoolData erstellt wurde
			Date createDate = poolData.getDate();
			//Berechnen der Differenz
			diff = date.getTime()-createDate.getTime();
			if(diff > this.lifeTime)
				this.poolDataList.removeFirst();
			else
				break;
		}
	}
	
	
	/**
	 * data entities stored in the PoolDataManager
	 */
	private class PoolData {
		private RemoteUser	remoteUser;
		private Date		date;
		
		public PoolData(RemoteUser user) {
			this.remoteUser = user;
			this.date = new Date();
		}
		
		public RemoteUser getRemoteUser() {
			return this.remoteUser;
		}
		
		public Date getDate() {
			return this.date;
		}
	}
}
