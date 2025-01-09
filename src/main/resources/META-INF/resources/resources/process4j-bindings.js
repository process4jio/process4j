const PANEL_OBJECT_BINDINGS = [
	{
		objProp: "nameEnabled",
		el: document.getElementById("po-name-input"),
		elAttr: "contenteditable"
	},{
		objProp: "name",
		el: document.getElementById("po-name-input"),
		elProp: "textContent",
		eType: "input",
		eVal: "outerText",
		callback: function(event) {
			if(isRootElement(event.objectId)) {
				bpmnModeler._definitions.rootElements[0].name = event.value;
				document.getElementById("processName").textContent = event.value;
			}
		}
	},{
		objProp: "type",
		callback: function(event) {
			document.querySelectorAll("span.node-type").forEach(function(i){
				if(i.classList.contains(event.value.split("bpmn:")[1].toLowerCase())) {
					i.classList.remove("d-none");
				} else {
					i.classList.add("d-none");
				}
			});
		}
	},{
		objProp: "implDisabled",
		el: document.getElementById("po-impl-input"),
		callback(event) {
			if(event.value) {
				event.element.setAttribute("disabled",true);
				event.element.value = "";
			} else {
				event.element.removeAttribute("disabled");
			}
		}
	},{
		objProp: "impl",
		el: document.getElementById("po-impl-input"),
		elProp: "value",
		eType: "input",
		eVal: "value",
		callback: function(event) {
			let bo = findBusinessObject(event.objectId);
		
			if(!bo) {
				return;
			}
		
			// Bootstrap documentation
			if(!bo.documentation) {
				bo.documentation = new Array();
			}
		
			// CRUD implementation
			let i = bo.documentation.findIndex(function(el){return el.id == 'p4j-implementation-for-'+bo.id});
			if(!event.value) {
				if(i != -1) {
					bo.documentation.splice(i,1);
				}
			}
			else if(i == -1) {
				bo.documentation.push(bpmnModeler._moddle.create("bpmn:Documentation",{text: event.value, id: "p4j-implementation-for-"+bo.id}));
			} else {
				bo.documentation[i].text = event.value;
				bo.documentation[i].id = "p4j-implementation-for-"+bo.id;
			}
		}
	},{
		objProp: "whileDisabled",
		el: document.getElementById("po-while-input"),
		callback(event) {
			if(event.value) {
				event.element.setAttribute("disabled",true);
				event.element.value = "";
			} else {
				event.element.removeAttribute("disabled");
			}
		}
	},{
		objProp: "whileCondition",
		el: document.getElementById("po-while-input"),
		elProp: "value",
		eType: "input",
		eVal: "value",
		callback: function(event) {
			let bo = findBusinessObject(event.objectId);
			
			if(!bo) {
				return;
			}
		
			// Bootstrap documentation
			if(!bo.documentation) {
				bo.documentation = new Array();
			}
		
			// CRUD while condition
			let i = bo.documentation.findIndex(function(el){return el.id == 'p4j-while-condition-for-'+bo.id});
			if(!event.value) {
				if(i != -1) {
					bo.documentation.splice(i,1);
				}
			}
			else if(i == -1) {
				bo.documentation.push(bpmnModeler._moddle.create("bpmn:Documentation",{text: event.value, id: "p4j-while-condition-for-"+bo.id}));
			} else {
				bo.documentation[i].text = event.value;
				bo.documentation[i].id = "p4j-while-condition-for-"+bo.id;
			}
		}
	},{
		objProp: "foreachDisabled",
		el: document.getElementById("po-foreach-input"),
		callback(event) {
			if(event.value) {
				event.element.setAttribute("disabled",true);
				event.element.value = "";
			} else {
				event.element.removeAttribute("disabled");
			}
		}
	},{
		objProp: "foreachExpression",
		el: document.getElementById("po-foreach-input"),
		elProp: "value",
		eType: "input",
		eVal: "value",
		callback: function(event) {
			let bo = findBusinessObject(event.objectId);
		
			if(!bo) {
				return;
			}
		
			// Bootstrap documentation
			if(!bo.documentation) {
				bo.documentation = new Array();
			}
			
			// CRUD foreach expression
			let i = bo.documentation.findIndex(function(el){return el.id == 'p4j-foreach-expression-for-'+bo.id});
			if(!event.value) {
				if(i != -1) {
					bo.documentation.splice(i,1);
				}
			}
			else if(i == -1) {
				bo.documentation.push(bpmnModeler._moddle.create("bpmn:Documentation",{text: event.value, id: "p4j-foreach-expression-for-"+bo.id}));
			} else {
				bo.documentation[i].text = event.value;
				bo.documentation[i].id = "p4j-foreach-expression-for-"+bo.id;
			}
		}
	},{
		objProp: "rulesDisabled",
		callback(event) {
			let el1 = document.getElementById("po-rules-resource-input");
			let el2 = document.getElementById("p4j-rules-key-input");
			let el3 = document.getElementById("edit-rules-button");
			if(event.value) {
				el1.setAttribute("disabled",true);
				el2.setAttribute("disabled",true);
				el3.setAttribute("disabled",true);
				el1.value = "";
				el2.value = "";
			} else {
				el1.removeAttribute("disabled");
				el2.removeAttribute("disabled");
				el3.removeAttribute("disabled");
			}
		}
	},{
		objProp: "rulesResource",
		el: document.getElementById("po-rules-resource-input"),
		elProp: "value",
		eType: "input",
		eVal: "value",
		callback: function(event) {
			let bo = findBusinessObject(event.objectId);
		
			if(!bo) {
				return;
			}
		
			// Bootstrap documentation
			if(!bo.documentation) {
				bo.documentation = new Array();
			}
			
			// CRUD rules
			let i = bo.documentation.findIndex(function(el){return el.id == 'p4j-rules-resource-for-'+bo.id});
			if(!event.value) {
				if(i != -1) {
					bo.documentation.splice(i,1);
				}
			}
			else if(i == -1) {
				bo.documentation.push(bpmnModeler._moddle.create("bpmn:Documentation",{text: event.value, id: "p4j-rules-resource-for-"+bo.id}));
			} else {
				bo.documentation[i].text = event.value;
				bo.documentation[i].id = "p4j-rules-resource-for-"+bo.id;
			}
		}
	},{
		objProp: "rulesKey",
		el: document.getElementById("p4j-rules-key-input"),
		elProp: "value",
		eType: "input",
		eVal: "value",
		callback: function(event) {
			let bo = findBusinessObject(event.objectId);
		
			if(!bo) {
				return;
			}
		
			// Bootstrap documentation
			if(!bo.documentation) {
				bo.documentation = new Array();
			}
			
			// CRUD rules
			let i = bo.documentation.findIndex(function(el){return el.id == 'p4j-rules-key-for-'+bo.id});
			if(!event.value) {
				if(i != -1) {
					bo.documentation.splice(i,1);
				}
			}
			else if(i == -1) {
				bo.documentation.push(bpmnModeler._moddle.create("bpmn:Documentation",{text: event.value, id: "p4j-rules-key-for-"+bo.id}));
			} else {
				bo.documentation[i].text = event.value;
				bo.documentation[i].id = "p4j-rules-key-for-"+bo.id;
			}
		}
	},{
		objProp: "documentationDisabled",
		el: document.getElementById("po-documentation-input"),
		callback(event) {
			if(event.value) {
				event.element.setAttribute("disabled",true);
				event.element.value = "";
			} else {
				event.element.removeAttribute("disabled");
			}
		}
	},{
		objProp: "documentation",
		el: document.getElementById("po-documentation-input"),
		elProp: "value",
		eType: "input",
		eVal: "value",
		callback: function(event) {
			let bo = findBusinessObject(event.objectId);
		
			if(!bo) {
				return;
			}
		
			// Bootstrap documentation
			if(!bo.documentation) {
				bo.documentation = new Array(); 
			}
		
			// CRUD documentation
			let i = bo.documentation.findIndex(function(el){return !el.id});
			if(!event.value) {
				if(i != -1) {
					bo.documentation.splice(i,1);
				}
			}
			else if(i == -1) {
				bo.documentation.push(bpmnModeler._moddle.create("bpmn:Documentation",{text: event.value}));
			} else {
				bo.documentation[i].text = event.value;
			}
		}
	}
];