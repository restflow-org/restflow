package org.restflow.beans;

import java.util.List;

import org.restflow.actors.ActorStatus;


public class ListDisassembler {

	private List<Object> _list;
	private Object _item;
	private int _index;
	private ActorStatus _actorStatus;
	
	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void initialize() {
		_index = 0;
		_list = null;
		_actorStatus.enableInput("list");
	}
	
	public void setList(List<Object> list) {
		_list = list;
	}
	
	public List<Object> getList() {
		return _list;
	}

	public int getIndex() {
		return _index;
	}
	
	public void setIndex(int i) {
		_index = i;
	}
	
	public void step() {

		if (_list != null) {
			_item = _list.get(_index++);
			if (_index >= _list.size()) {
				_index = 0;
				_list = null;
				_actorStatus.enableInput("list");				
			}
			return;
		}
	}
	
	public Object getItem() {
		return _item;
	}
	
}