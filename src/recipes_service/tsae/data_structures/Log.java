/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import recipes_service.data.Operation;
//LSim logging system imports sgeag@2017
//import lsim.coordinator.LSimCoordinator;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.library.api.LSimLogger;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias December 2012
 *
 */
public class Log implements Serializable {
	// Only for the zip file with the correct solution of phase1.Needed for the
	// logging system for the phase1. sgeag_2018p
//	private transient LSimCoordinator lsim = LSimFactory.getCoordinatorInstance();
	// Needed for the logging system sgeag@2017
//	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -4864990265268259700L;
	/**
	 * This class implements a log, that stores the operations received by a client.
	 * They are stored in a ConcurrentHashMap (a hash table), that stores a list of
	 * operations for each member of the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log = new ConcurrentHashMap<String, List<Operation>>();

	public Log(List<String> participants) {
		// create an empty log
		for (Iterator<String> it = participants.iterator(); it.hasNext();) {
			log.put(it.next(), new Vector<Operation>());
		}
	}

	/**
	 * inserts an operation into the log. Operations are inserted in order. If the
	 * last operation for the user is not the previous operation than the one being
	 * inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op) {
		// ....
		String host = op.getTimestamp().getHostid();
		List<Operation> operations = this.log.get(host);

		if (operations.isEmpty()
				|| operations.get(operations.size() - 1).getTimestamp().compare(op.getTimestamp()) < 0) {
			operations.add(op);
			log.put(host, operations);
			return true;
		}

		// return generated automatically. Remove it when implementing your solution
		return false;
	}

	/**
	 * Checks the received summary (sum) and determines the operations contained in
	 * the log that have not been seen by the proprietary of the summary. Returns
	 * them in an ordered list.
	 * 
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum) {

		// return generated automatically. Remove it when implementing your solution
		List<Operation> newerList = new Vector<Operation>();

		for (ConcurrentHashMap.Entry<String, List<Operation>> entry : log.entrySet()) {
			String hostId = entry.getKey();
			List<Operation> messages = entry.getValue();
			if (!messages.isEmpty()) {
				Timestamp last = messages.get(messages.size() - 1).getTimestamp();
				if (last.compare(sum.getLast(hostId)) > 0) {
					if (sum.getLast(hostId).isNullTimestamp()) {
						newerList.addAll(messages);
					} else {
						for (Operation op : messages) {
							if (op.getTimestamp().compare(sum.getLast(hostId)) > 0)
								newerList.add(op);
						}
					}
				}
			}
		}
		return newerList;
	}

	/**
	 * Removes from the log the operations that have been acknowledged by all the
	 * members of the group, according to the provided ackSummary.
	 * 
	 * @param ack: ackSummary.
	 */
	public void purgeLog(TimestampMatrix ack) {
	}

	/**
	 * equals
	 */
	@Override
	public synchronized boolean equals(Object obj) {

		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Log other = (Log) obj;

		if (!other.log.equals(this.log)) {
			return false;
		} else {
			return this.log.equals(other.log);
		}
		// return generated automatically. Remove it when implementing your solution
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name = "";
		for (Enumeration<List<Operation>> en = log.elements(); en.hasMoreElements();) {
			List<Operation> sublog = en.nextElement();
			for (ListIterator<Operation> en2 = sublog.listIterator(); en2.hasNext();) {
				name += en2.next().toString() + "\n";
			}
		}

		return name;
	}

	public synchronized List<Operation> msgList(String pid, Timestamp first) {
		List<Operation> messages = new Vector<Operation>();

		for (Operation op : log.get(pid)) {
			if (op.getTimestamp().compare(first) > 0)
				messages.add(op);
		}

		return messages;

	}
}
