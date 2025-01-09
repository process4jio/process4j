/*
* Process4j
* 
* Made of
*   bpmn-js
*   canvg
*   Bootstrap
*   Font Awesome
*/

let settings;

function loadSettings() {
	let settings = JSON.parse(localStorage.getItem("process4j.settings.json"));
	if(!settings) {
		settings = new Object();
	}
	return Object.assign(DEFAULTS,settings);
};

function saveSettings(settings) {
	localStorage.setItem("process4j.settings.json",JSON.stringify(settings));
};

function bind(options) {
	let bVal = options.obj[options.objProp];
	Object.defineProperty(options.obj, options.objProp, {
		get() {return bVal;},
		set(newValue) {
			bVal = newValue;
			if(options.elProp) {
				options.el[options.elProp] = newValue;
			}
			if(options.elAttr) {
				options.el.setAttribute(options.elAttr,newValue);
			}
			if(options.callback) {
				options.callback({
					objectId: options.obj.id,
					element: options.el,
					value: newValue
				});	
			}
		},
		enumerable: true,
		configurable: true
	});
	if(options.eType) {
		options.el.addEventListener(options.eType,function(e){
			options.obj[options.objProp] = e.target[options.eVal];
		});
	}
	if(options.elProp) {
		options.el[options.elProp] = options.obj[options.objProp];
	}
	if(options.elAttr) {
		options.el.setAttribute(options.elAttr,options.obj[options.objProp]);
	}
};

// Toggle views, i.e. hide and show stuff
function toggleBpmnRules() {
	Array.from(document.querySelectorAll(".bpmn,.rules")).forEach(function(el){
		el.classList.toggle("d-none");	
	});
};

/*
* BPMN Modeler
*  
*/

const URL_OTHER = "http://bpmn.process4j.io/";
const EDITABLE_TYPES = ["bpmn:Process","bpmn:Task","bpmn:BusinessRuleTask","bpmn:SubProcess","bpmn:ExclusiveGateway"];
const AVAILABLE_EVENTS = [
  'element.hover',
  'element.out',
  'element.click',
  'element.dblclick',
  'element.mousedown',
  'element.mouseup'
];
const DEFAULTS = {
	panel: {
		sticky: true
	}
};
const events = [
  'element.click'
];

// Variables
let bpmnModeler;
let eventBus;

// Panel object
let po = {
	id: "",
	nameEnabled: false,
	name: "",
	type: "",
	implDisabled: true,
	impl: "",
	whileDisabled: true,
	whileCondition: "",
	foreachDisabled: true,
	foreachExpression: "",
	rulesDisabled: false,
	rulesResource: "",
	rulesKey: "",
	documentationDisabled: true,
	documentation: "" 
};

async function exportBPMN() {
	let result;
	if(!bpmnModeler._definitions) {
		result = {
			xml: ""
		};
	} else {
		result = await bpmnModeler.saveXML({ format: true });
	}
	//console.log('DIAGRAM', result.xml);
	return window.URL.createObjectURL(new Blob([result.xml], { type: 'application/xml;charset="utf-8"' }));
};

async function exportSVG() {
	let result = await bpmnModeler.saveSVG({ format: true });
	//console.log('DIAGRAM', result.svg);
	return window.URL.createObjectURL(new Blob([result.svg], { type: 'image/svg+xml;charset="utf-8"' }));
};

async function exportPNG(link) {
	let result = await bpmnModeler.saveSVG({ format: true });
	const canvas = document.querySelector('canvas');
	const ctx = canvas.getContext('2d');
	let v = canvg.Canvg.fromString(ctx, result.svg);
	v.start();
	return canvas.toDataURL("image/png");
	/*canvas.toBlob(function(blob){
		link = window.URL.createObjectURL(blob);
	});*/
};

async function downloadBPMN() {
	let link = await exportBPMN();
	const a = document.createElement("a");
	a.href = link;
	a.setAttribute("download",(bpmnModeler._definitions ? bpmnModeler._definitions.id : "") + ".bpmn");
	await a.click();
};

async function downloadSVG() {
	let link = await exportSVG();
	const a = document.createElement("a");
	a.href = link;
	a.setAttribute("download",(bpmnModeler._definitions ? bpmnModeler._definitions.id : "") + ".svg");
	await a.click();
};

async function downloadPNG() {
	let link = await exportPNG();
	const a = document.createElement("a");
	a.href = link;
	a.setAttribute("download",(bpmnModeler._definitions ? bpmnModeler._definitions.id : "") + ".png");
	await a.click();
};

async function previewBPMN() {
	let link = await exportBPMN();
	window.open(link);
};

async function previewSVG() {
	let link = await exportSVG();
	window.open(link);
}

async function load(bpmnXML) {

	// import diagram
	try {

	    await bpmnModeler.importXML(bpmnXML);

		syncBoToPo(bpmnModeler._definitions.rootElements[0]);

	    // access modeler components
	    let canvas = bpmnModeler.get('canvas');
		let overlays = bpmnModeler.get('overlays');
		
	    // zoom to fit full viewport
	    canvas.zoom('fit-viewport');

	} catch (err) {

	    console.error('could not import BPMN 2.0 diagram', err);
	}
};


function openDiagram(file) {
	if(file.type && file.type.indexOf("text/xml") === -1) {
		console.warn("File type '%s' is not xml", file.type);
		openRules(file);
		return;
	}
	
	const reader = new FileReader();
	
	reader.onloadend = function() {
		load(reader.result);
	}
	
	reader.readAsText(file);
};

/*function autoConfigSourceResource() {
	return "config/" + getName().replace(/\s/g, "").toLowerCase() + ".config.json";
};*/

function newDiagram() {
	
	// Namespaces
	let xsi = "http://www.w3.org/2001/XMLSchema-instance";
	let bpmn = "http://www.omg.org/spec/BPMN/20100524/MODEL";
	let bpmndi = "http://www.omg.org/spec/BPMN/20100524/DI";
	let dc = "http://www.omg.org/spec/DD/20100524/DC";
	let targetNS = "http://bpmn.process4j.io/";
		
	// Bootstrap
	let id = uuidv4().substring(0,7);
	let definitionsId = "Definitions_" + id;
	let definitionsName = definitionsId;
	let processId = "Process_" + id;
	let processName = "Starter";
	let processImpl = "com.acme.StarterProcess";
	let startEventId = "Event_" + id;
	let diagramId = "BPMNDiagram_" + id;
	let planeId = "BPMNPlane_" + id	
	let startEventShapeId = "BPMNShape_" + startEventId;
	
	let xml = 	'<?xml version="1.0" encoding="UTF-8"?>'+
				'<bpmn:definitions xmlns:xsi="'+xsi+'" xmlns:bpmn="'+bpmn+'" xmlns:bpmndi="'+bpmndi+'" xmlns:dc="'+dc+'" id="'+definitionsId+'" name="'+definitionsName+'" targetNamespace="'+targetNS+'">'+
				'<bpmn:process id="'+processId+'" name="'+processName+'"><bpmn:documentation id="p4j-implementation-for-'+processId+'">'+processImpl+'</bpmn:documentation><bpmn:startEvent id="'+startEventId+'" name="Start"/>'+
				'</bpmn:process><bpmndi:BPMNDiagram id="'+diagramId+'">'+
				'<bpmndi:BPMNPlane id="'+planeId+'" bpmnElement="'+processId+'">'+
				'<bpmndi:BPMNShape id="'+startEventShapeId+'" bpmnElement="'+startEventId+'">'+
				'<dc:Bounds x="156" y="81" width="36" height="36" />'+
				'</bpmndi:BPMNShape>'+
				'</bpmndi:BPMNPlane>'+
				'</bpmndi:BPMNDiagram>'
				+'</bpmn:definitions>';
				
	load(xml);
};

function isRootElement(id) {
	return bpmnModeler._definitions.rootElements[0].id == id;
};

function findBusinessObject(id) {
	if(!bpmnModeler._definitions || !bpmnModeler._definitions.rootElements[0]) {
		return null;
	}
	let rootElement = bpmnModeler._definitions.rootElements[0];
	return isRootElement(id) ? rootElement : rootElement.flowElements.filter(function(e){return e.id == id})[0];
};

function getDocumentation(bo) {
	if(!bo.documentation) {
		return "";
	}
	
	let documentations = bo.documentation.filter(function(el){return !el.id});
	return documentations.length == 0 ? "" : documentations[0].text;
};

function getImplementation(bo) {
	if(!bo.documentation) {
		return "";
	}
	
	let implementations = bo.documentation.filter(function(el){return el.id == 'p4j-implementation-for-'+bo.id});
	return implementations.length == 0 ? "" : implementations[0].text;
};

function getWhileCondition(bo) {
	if(!bo.documentation) {
		return "";
	}
	
	let whiles = bo.documentation.filter(function(el){return el.id == 'p4j-while-condition-for-'+bo.id});
	return whiles.length == 0 ? "" : whiles[0].text;
};

function getForeachExpression(bo) {
	if(!bo.documentation) {
		return "";
	}
	
	let foreaches = bo.documentation.filter(function(el){return el.id == 'p4j-foreach-expression-for-'+bo.id});
	return foreaches.length == 0 ? "" : foreaches[0].text;
};

function getRulesResource(bo) {
	if(!bo.documentation) {
		return "";
	}
	
	let rules = bo.documentation.filter(function(el){return el.id == 'p4j-rules-resource-for-'+bo.id});
	return rules.length == 0 ? "" : rules[0].text;
};

function getRulesKey(bo) {
	if(!bo.documentation) {
		return "";
	}
	
	let rules = bo.documentation.filter(function(el){return el.id == 'p4j-rules-key-for-'+bo.id});
	return rules.length == 0 ? "" : rules[0].text;
};

function togglePanel() {
	document.getElementById("panel").classList.toggle("open");
	document.getElementById("body").classList.toggle("panel");
};

function openPanel() {
	document.getElementById("panel").classList.add("open");
	document.getElementById("body").classList.add("panel");
};

function closePanel() {
	document.getElementById("panel").classList.remove("open");
	document.getElementById("body").classList.remove("panel");
};

/*function viewBPMN() {
	toggleBpmnRules();
};*/

/*function viewRules() {
	//document.getElementById("body").style.marginRight = "0";
	toggleBpmnRules();
};*/

// Synchronizing a business object (bo) with the panel object (po)
function syncBoToPo(bo) {
	po.id = bo.id;
	po.nameEnabled = bo.$type == "bpmn:Process";
	po.name = bo.name;
	po.type = bo.$type;
	po.implDisabled = false;
	po.impl = getImplementation(bo);
	po.whileDisabled = !bo.loopCharacteristics || bo.loopCharacteristics.$type != "bpmn:StandardLoopCharacteristics";
	po.whileCondition = getWhileCondition(bo);
	po.foreachDisabled = !bo.loopCharacteristics || bo.loopCharacteristics.$type != "bpmn:MultiInstanceLoopCharacteristics" || !bo.loopCharacteristics.isSequential;
	po.foreachExpression = getForeachExpression(bo);
	po.rulesDisabled = bo.$type != "bpmn:BusinessRuleTask";
	po.rulesResource = getRulesResource(bo);
	po.rulesKey = getRulesKey(bo);
	po.documentationDisabled = false;
	po.documentation = getDocumentation(bo);
};

/*
* Rules Editor
*
*/

let loadedConfig = new Object();
let configs = new Array();
let filename = "rules.json"

function createRuleCRUDElement(rule,config) {
	
	let inputGroupEl = document.createElement("div");
	inputGroupEl.setAttribute("id",uuidv4());
	inputGroupEl.classList.add("input-group","input-group-sm","mb-1");
	inputGroupEl.setAttribute("draggable",true);
	inputGroupEl.addEventListener("dragstart",function(e){
		e.stopPropagation();
		let data = new Object();
		data.id = e.target.id;
		data.clientY = e.clientY;
		data.position = rule.position;
		data.config = rule.config;
		data.type = "rule";
		e.dataTransfer.setData("text", JSON.stringify(data));
	});
	inputGroupEl.addEventListener("drop",function(e){
		e.preventDefault();
		let data = JSON.parse(e.dataTransfer.getData("text"));
		let droppedEl = document.getElementById(data.id);
			
		// No-op
		if(this.id == droppedEl.id || data.type != "rule") {
			return;
		}
		
		console.log("rule:",rule);
		
		// Runaway config
		if(rule.config != data.config) {
			console.warn("Blocking illegal drop! I.e attempt to drop a rule into another rule list");
			return;
		}
							
		// Insert dropped after this
		if(data.clientY < e.clientY) {
			if(this.parentElement.lastElementChild.id == this.id) {
				this.parentElement.appendChild(droppedEl);
			} else {
				this.parentElement.insertBefore(droppedEl,this.nextElementSibling);
			}
		}
					
		// Insert dropped before this
		else {
			this.parentElement.insertBefore(droppedEl,this);
		}
		
		rule.move(data.position);
	});
	
	let infoButtonEl = document.createElement("button");
	infoButtonEl.classList.add("btn","btn-outline-secondary");
	infoButtonEl.setAttribute("type","button");
	infoButtonEl.setAttribute("title","Show rule description");
	infoButtonEl.innerHTML = '<i class="fas fa-info"></i>';
	infoButtonEl.addEventListener("click",function(e){
		inputGroupEl.querySelector(".rule-description").classList.toggle("d-none");
	});
	inputGroupEl.appendChild(infoButtonEl);
	
	let expressionInputEl = document.createElement("input");
	expressionInputEl.classList.add("form-control","rule-expression");
	expressionInputEl.setAttribute("type","text");
	expressionInputEl.setAttribute("spellcheck",false);
	expressionInputEl.setAttribute("placeholder","Expression");
	expressionInputEl.setAttribute("value",rule.expression);
	expressionInputEl.addEventListener("change",function(e){
		rule.expression = e.target.value;
	});
	inputGroupEl.appendChild(expressionInputEl);
	
	let spanEl = document.createElement("span");
	spanEl.classList.add("input-group-text");
	spanEl.innerHTML = '<i class="fas fa-arrow-right"></i>';
	inputGroupEl.appendChild(spanEl);
	
	let resultInputEl = document.createElement("input");
	resultInputEl.classList.add("form-control","rule-result");
	resultInputEl.setAttribute("type","text");
	resultInputEl.setAttribute("spellcheck",false);
	resultInputEl.setAttribute("placeholder","Result");
	resultInputEl.setAttribute("value",rule.result);
	resultInputEl.addEventListener("change",function(e){
		rule.result = e.target.value;
	});
	inputGroupEl.appendChild(resultInputEl);
	
	let buttonEl = document.createElement("button");
	buttonEl.classList.add("btn","btn-outline-secondary");
	buttonEl.setAttribute("type","button");
	buttonEl.setAttribute("title","Delete rule");
	buttonEl.innerHTML = '<i class="fas fa-minus"></i>';
	buttonEl.addEventListener("click",function(e){
		config.rules.splice(rule.position, 1);
		inputGroupEl.remove();
	});
	inputGroupEl.appendChild(buttonEl);
	
	let descrInputEl = document.createElement("input");
	descrInputEl.classList.add("form-control","rule-description");
	let switchEl = document.getElementById("info-switch-"+config.id);
	if(!switchEl || !switchEl.checked) {
		descrInputEl.classList.add("d-none");
	}
	descrInputEl.setAttribute("type","text");
	descrInputEl.setAttribute("spellcheck",false);
	descrInputEl.setAttribute("placeholder","Om ... så ...");
	descrInputEl.setAttribute("value",rule.description ? rule.description : "");
	descrInputEl.addEventListener("change",function(e){
		rule.description = e.target.value;
	});
	inputGroupEl.appendChild(descrInputEl);
	
	return inputGroupEl;
}

function createRuleCRUD(rule,config) {
						
	let inputGroupEl = createRuleCRUDElement(rule,config);
						
	let bExpression = rule.expression;
	Object.defineProperty(rule, "expression", {
		get() {return bExpression;},
		set(newValue) {
			bExpression = newValue;
			inputGroupEl.querySelector(".rule-expression").value = newValue;
		},
		enumerable: true,
		configurable: true
	});
			
	let bResult = rule.result;
	Object.defineProperty(rule, "result", {
		get() {return bResult;},
		set(newValue) {
			bResult = newValue;
			inputGroupEl.querySelector(".rule-result").value = newValue;
		},
		enumerable: true,
		configurable: true
	});
	
	let bDescription = rule.description;
	Object.defineProperty(rule, "description", {
		get() {return bDescription;},
		set(newValue) {
			bDescription = newValue;
			inputGroupEl.querySelector(".rule-description").value = newValue;
		},
		enumerable: true,
		configurable: true
	});
	
	rule.move = function(droppedRulePosition) {
		
		let reordered = new Array(config.rules.length); 
		
		let movement = this.position - droppedRulePosition;
		
		// Increment index on rules passed by movement 		
		if(movement < 0) {
			reordered[this.position] = config.rules[droppedRulePosition];
			for(var i=this.position+1;i<=droppedRulePosition;++i) {
				reordered[i] = config.rules[i-1];
			}
		}
		
		// Decrement index on rules passed by movement
		else {
			reordered[this.position] = config.rules[droppedRulePosition];
			for(var i=droppedRulePosition;i<this.position;++i) {
				reordered[i] = config.rules[i+1];
			}
		}
		
		// Keep the rest as is
		for(var i=0;i<reordered.length;++i) {
			if(reordered[i] === undefined) {
				continue;
			}
			config.rules[i] = reordered[i];
		}
	};
	
	Object.defineProperty(rule, "position", {
		get() {
			return config.rules.findIndex(function(r) {
				return r.expression === rule.expression && r.result === rule.result;
			});
		},
		enumerable: false,
		configurable: true
	});
	
	Object.defineProperty(rule, "config", {
		get() {
			return config.pointer;
		},
		enumerable: false,
		configurable: true
	});
	
	return inputGroupEl;
}

function createRule(config) {
	let rule = new Object();
	rule.expression = "";
	rule.result = "";
	rule.description = "";
	return rule;
}

function createRulesElement(config) {
	
	let rulesEl = document.createElement("div");
	rulesEl.classList.add("rules");
					
	let ruleListEl = document.createElement("div");
	ruleListEl.classList.add("rule-list");
	ruleListEl.addEventListener("dragover",function(e){
		e.preventDefault();
		e.stopPropagation();
	});
	ruleListEl.addEventListener("drop",function(e){
		e.preventDefault();
		e.stopPropagation();
	});
					
	let addButtonEl = document.createElement("button");
	addButtonEl.setAttribute("type","button");
	addButtonEl.setAttribute("title","Add New Rule");
	addButtonEl.innerHTML = '<i class="fas fa-plus"></i>';
	addButtonEl.classList.add("btn","btn-sm","btn-outline-secondary","mb-3");
	addButtonEl.addEventListener("click",function(e){
		let newRule = createRule();
		config.rules.unshift(newRule);
		if(ruleListEl.firstElementChild) {
			ruleListEl.insertBefore(createRuleCRUD(newRule,config),ruleListEl.firstElementChild);
		} else {
			ruleListEl.appendChild(createRuleCRUD(newRule,config));
		}
	});
	rulesEl.appendChild(addButtonEl);
	
	let infoEl = document.createElement("div");
	infoEl.setAttribute("title","Toggle descriptions");
	infoEl.classList.add("form-check", "form-switch","d-inline-block","mb-3","ms-3");
	
	let infoSwitchEl = document.createElement("input");
	infoSwitchEl.setAttribute("id","info-switch-"+config.pointer);
	infoSwitchEl.setAttribute("type","checkbox");
	infoSwitchEl.classList.add("form-check-input");
	infoSwitchEl.addEventListener("click",function(e){
		let all = rulesEl.querySelectorAll(".rule-description");
		var i;
		if(e.target.checked) {
			for (i = 0; i < all.length; i++) {
 				all[i].classList.remove("d-none");
			}
		} else {
			for (i = 0; i < all.length; i++) {
 				all[i].classList.add("d-none");
			}
		}
	});
	infoEl.appendChild(infoSwitchEl);
	
	let infoLabelEl = document.createElement("label");
	infoLabelEl.setAttribute("for","info-switch-"+config.pointer);
	infoLabelEl.classList.add("form-check-label");
	infoLabelEl.textContent = "Descriptions";
	infoEl.appendChild(infoLabelEl);

	rulesEl.appendChild(infoEl);

	rulesEl.appendChild(ruleListEl);
				
	config.rules.forEach(function(rule){
		ruleListEl.appendChild(createRuleCRUD(rule,config));
	});
					
	let addButtonEl2 = document.createElement("button");
	addButtonEl2.setAttribute("type","button");
	addButtonEl2.setAttribute("title","Add New Rule");
	addButtonEl2.innerHTML = '<i class="fas fa-plus"></i>';
	addButtonEl2.classList.add("btn","btn-sm","btn-outline-secondary","mt-3");
	addButtonEl2.addEventListener("click",function(e){
		let newRule = createRule();
		config.rules.push(newRule);
		ruleListEl.appendChild(createRuleCRUD(newRule,config));
	});
	rulesEl.appendChild(addButtonEl2);
	
	return rulesEl;
}

function createWidget(config) {
	
	Object.defineProperty(config, "position", {
		get() {
			return configs.findIndex(function(c) {
				return c.pointer === config.pointer;
			});
		},
		enumerable: false,
		configurable: true
	});
	
	config.move = function(droppedPosition) {
		
		let reordered = new Array(configs.length); 
		
		let movement = this.position - droppedPosition;
		
		// Increment index on configs passed by movement 		
		if(movement < 0) {
			reordered[this.position] = configs[droppedPosition];
			for(var i=this.position+1;i<=droppedPosition;++i) {
				reordered[i] = configs[i-1];
			}
		}
		
		// Decrement index on configs passed by movement
		else {
			reordered[this.position] = configs[droppedPosition];
			for(var i=droppedPosition;i<this.position;++i) {
				reordered[i] = configs[i+1];
			}
		}
		
		// Keep the rest as is
		for(var i=0;i<reordered.length;++i) {
			if(reordered[i] === undefined) {
				continue;
			}
			configs[i] = reordered[i];
		}
	};
	
	let columnEl = document.createElement("div");
	columnEl.classList.add("col-12");
	columnEl.classList.add("node");
	//columnEl.classList.add(config.p4j_immutable.type);
	//columnEl.setAttribute("id","widget-"+config.p4j_immutable.id);
	columnEl.setAttribute("id","widget-"+config.pointer);
	columnEl.setAttribute("draggable",true);
	columnEl.addEventListener("dragstart",function(e){
		let data = new Object();
		data.id = e.target.id;
		data.clientY = e.clientY;
		data.position = config.position;
		data.type = "widget";
		e.dataTransfer.setData("text", JSON.stringify(data));
	});
	columnEl.addEventListener("drop",function(e){
		e.preventDefault();
		let data = JSON.parse(e.dataTransfer.getData("text"));
		var droppedEl = document.getElementById(data.id);
			
		// No-op
		if(this.id == droppedEl.id || data.type != "widget") {
			return;
		}
			
		// Insert dropped after this
		if(data.clientY < e.clientY) {
			if(this.parentElement.lastElementChild.id == this.id) {
				this.parentElement.appendChild(droppedEl);
			} else {
				this.parentElement.insertBefore(droppedEl,this.nextElementSibling);
			}
		}
					
		// Insert dropped before this
		else {
			this.parentElement.insertBefore(droppedEl,this);
		}
		
		config.move(data.position);
	});
				
	let cardEl = document.createElement("div");
	cardEl.classList.add("card","border-secondary","mt-3");
	columnEl.appendChild(cardEl);
				
	let cardBodyEl = document.createElement("div");
	cardBodyEl.classList.add("card-body","text-secondary");
	cardEl.appendChild(cardBodyEl);

	let cardTitleEl = document.createElement("h5");
	cardTitleEl.classList.add("card-title","mb-1");
	let pointerEl = document.createElement("span");
	pointerEl.setAttribute("contenteditable",true);
	pointerEl.textContent = config.pointer;
	cardTitleEl.appendChild(pointerEl);
	cardBodyEl.appendChild(cardTitleEl);
	
	bind({
		obj: config,
		objProp: "pointer",
		el: pointerEl,
		elProp: "textContent",
		eType: "input",
		eVal: "outerText",
		callback: function(event) {
			//po.rulesKey = event.value; // TODO: This is a special scenario. Cannot assume panel object.
		}
	});
	
	let deleteIconEl = document.createElement("i");
	deleteIconEl.classList.add("fas","fa-trash-alt","float-end");
	deleteIconEl.style.cursor = "pointer";
	deleteIconEl.addEventListener("click",function(e){
		configs.splice(config.position,1);
		columnEl.remove();
	});
	cardTitleEl.appendChild(deleteIconEl)

	/*let cardSubTitleEl = document.createElement("div");
	cardSubTitleEl.classList.add("card-subtitle","text-muted","mb-3");
	cardSubTitleEl.style.fontSize = "0.75rem";
	cardSubTitleEl.textContent = config.p4j_immutable.impl;
	cardBodyEl.appendChild(cardSubTitleEl);*/
	
	/*let cardDocumentationEl = document.createElement("div");
	cardDocumentationEl.classList.add("mb-3");
	cardDocumentationEl.innerHTML = '<label for="documentation-textarea-'+config.p4j_immutable.id+'" class="form-label">Documentation</label>';
	cardBodyEl.appendChild(cardDocumentationEl);
	
	let documentationTextareaEl = document.createElement("textarea");
	documentationTextareaEl.classList.add("form-control","form-control-sm");
	documentationTextareaEl.setAttribute("id","documentation-textarea-"+config.p4j_immutable.id);
	documentationTextareaEl.value = config.p4j_documentation;
	documentationTextareaEl.addEventListener("change",function(e){
		config.p4j_documentation = e.target.value;
	});
	cardDocumentationEl.appendChild(documentationTextareaEl);
		
	var bDocumentation = config.p4j_documentation;
	Object.defineProperty(config, "p4j_documentation", {
		get() {return bDocumentation;},
		set(newValue) {
			bDocumentation = newValue;
			documentationTextareaEl.value = newValue;
		},
		enumerable: true,
		configurable: true
	});*/
	
	let cardFooterEl = document.createElement("div");
	cardFooterEl.classList.add("card-footer");
	cardEl.appendChild(cardFooterEl);

	/*if(config.p4j_immutable.configSource) {
		let configSourceEl = document.createElement("div");
		configSourceEl.classList.add("text-muted","float-end","small");
		cardFooterEl.appendChild(configSourceEl);
		
		let configSourceResourceIconEl = document.createElement("i");
		configSourceResourceIconEl.classList.add("far","fa-file");
		configSourceEl.appendChild(configSourceResourceIconEl);
		
		let configSourceResourceEl = document.createElement("span");
		configSourceResourceEl.classList.add("ms-1");
		configSourceResourceEl.textContent = config.p4j_immutable.configSource.resource + " '" + config.p4j_immutable.configSource.fqn + "'";
		configSourceEl.appendChild(configSourceResourceEl);
	}*/
				
	/*let howtoEl = document.createElement("div");
	howtoEl.classList.add("text-muted");
	cardFooterEl.appendChild(howtoEl);

	let howtoIconEl = document.createElement("i");
	howtoIconEl.classList.add("far","fa-question-circle");
	howtoEl.appendChild(howtoIconEl);
	
	howtoIconEl.setAttribute("title",config.p4j_howto.title);
		
	let howtoTitleEl = document.createElement("span");
	howtoTitleEl.classList.add("ms-1");
	howtoTitleEl.textContent = config.p4j_howto.title;
	howtoEl.appendChild(howtoTitleEl);*/
				
/*	let modifiedEl = document.createElement("div");
	modifiedEl.classList.add("text-muted","small");
	modifiedEl.textContent = config.modified ? "modified: " + config.modified : "";
	cardFooterEl.appendChild(modifiedEl);*/
	
	
	
	cardBodyEl.appendChild(createRulesElement(config));
	
	/*switch(config.p4j_immutable.type) {
		case "DECISION_TABLE":
			cardSubTitleEl.innerHTML = '<i class="fas fa-gavel" title="Decision Table"></i> ' + cardSubTitleEl.textContent;
			cardBodyEl.appendChild(createRulesElement(config));
		break;
		case "PROCESS":
			cardSubTitleEl.innerHTML = '<i class="fas fa-cogs" title="Process"></i> ' + cardSubTitleEl.textContent;
		break;
		case "TASK":
			cardSubTitleEl.innerHTML = '<i class="fas fa-wrench" title="Task"></i> ' + cardSubTitleEl.textContent;
		break;
		case "EXCLUSIVE_GATEWAY":
			cardSubTitleEl.innerHTML = '<i class="fas fa-directions" title="Gateway"></i> ' + cardSubTitleEl.textContent;
		break;
	}*/
	
	return columnEl;
}

async function config() {
	let config = new Object();
	configs.forEach(function(conf){
		config[conf.pointer] = conf.rules;
	});
	console.log(config);
	return config;
}

async function exportConfig() {
	let obj = await config();
	return window.URL.createObjectURL(new Blob([JSON.stringify(obj,null,2)], { type: 'application/json;charset="utf-8"' }));
}

async function downloadConfig() {
	let link = await exportConfig();
	const a = document.createElement("a");
	a.href = link;
	console.log(filename);
	a.setAttribute("download",filename);
	console.log(a);
	await a.click();
}

async function previewConfig() {
	let link = await exportConfig();
	window.open(link);
}

async function render() {
	let widgetsEl = document.getElementById("widgets");
	widgetsEl.innerHTML = "";
	widgetsEl.addEventListener("dragover",function(e){
		e.preventDefault();
	});
	widgetsEl.addEventListener("drop",function(e){
		e.preventDefault();
	});
	configs.forEach(function(conf){
		
		console.log("creating widget for conf: ", conf);
		
		let widgetEl = createWidget(conf);
		widgetsEl.appendChild(widgetEl);
	});
};

async function loadRules(configJSON) {
	
	loadedConfig = JSON.parse(configJSON);
	configs = new Array();
	
	if(typeof loadedConfig != "object" || Array.isArray(loadedConfig)) {
		console.warn("File content \"%s..\" is not a valid json object", configJSON.substring(0,2));
		return;
	}
	
	for(c in loadedConfig) {
		let wrapper = new Object();
		wrapper.rules = loadedConfig[c];
		wrapper.pointer = c;
		configs.push(wrapper);
	}
	render();
};

function isNotJSON(val) {
	if(typeof val == 'string') {
		if(val.indexOf("{") != 0 && val.indexOf("[") != 0) {
			return true;
		}
	}
	return false;
};

function newRules(options) {
	if(!configs) {
		configs = new Array();
	}

	let key;
	if(options) {
		key = options.key;
		if(options.resource) {
			filename = options.resource.substring(options.resource.lastIndexOf("/")+1);
		}
	}

	// Rules exist in resource	
	if(configs.filter(function(c) { return c.pointer == key;}).length != 0) {
		return;
	}
	
	let wrapper = new Object();
	wrapper.rules = new Array();
	wrapper.pointer = (key ? key : uuidv4().split("-")[0]);
	configs.push(wrapper);
	render();
};

function wipeRules() {
	loadedConfig = new Object();
	configs = new Array();
	render();
};

function openRules(file) {
	if(file.type.indexOf("application/json") === -1) {
		console.warn("File is not json", file.type, file);
		return;
	}
	
	const reader = new FileReader();
	
	reader.onloadend = function() {
		if(isNotJSON(reader.result)) {
			console.warn("File content \"%s..\" is not valid json", reader.result.substring(0,2));
			return;
		}
		loadRules(reader.result);
		filename = file.name;
	}
	
	reader.readAsText(file);
};

// Ready
document.onreadystatechange = function() {
	
	// Document finished loading
	if(document.readyState === 'interactive') {
    	
		// Load settings
		settings = loadSettings();

		// BPMN: Create BPMN Modeler
		bpmnModeler = new BpmnJS({
			container : '#canvas',
			keyboard : {
		    	bindTo : window
			}
    	});

		// BPMN: Register process4j package
		// bpmnModeler._moddle.registry.registerPackage(P4J_PKG);

		// BPMN: Panel object data bindings
		PANEL_OBJECT_BINDINGS.forEach(function(b){
			bind(Object.assign(b,{obj: po}));
		});
		
		// BPMN: Get event bus
		eventBus = bpmnModeler.get('eventBus');
		
		// BPMN: Add event bus listeners
		events.forEach(function(event) {
			eventBus.on(event, function(e){
				
				//console.log(e);
				
				if(EDITABLE_TYPES.includes(e.element.type)) {
					
					let bo = e.element.businessObject;
					
					// Clicked on the selected element 
					if(po.id == e.element.id && !settings.panel.sticky) {
						togglePanel();							
					} else if(!settings.panel.sticky) {
						openPanel();
					}
					
					syncBoToPo(bo);
				}
			});
		});
		
		// BPMN: Add event listeners
		document.getElementById("bpmn-rules-toggle-button").addEventListener("click",function(e){
			toggleBpmnRules();
		});
		
		document.getElementById("bpmn-new-button").addEventListener("click",function(e){
			newDiagram();
		});
		
		document.getElementById("bpmn-open-button").addEventListener("click",function(e){
			document.getElementById("bpmn-upload").click();
		});	
		
		document.getElementById("bpmn-preview-button").addEventListener("click",function(e){
			previewBPMN();
		});	
		
		document.getElementById("bpmn-download-button").addEventListener("click",function(e){
			downloadBPMN();
		});	
		
		document.getElementById("bpmn-preview-svg-button").addEventListener("click",function(e){
			previewSVG();
		});	
		
		document.getElementById("bpmn-download-svg-button").addEventListener("click",function(e){
			downloadSVG();
		});
		
		document.getElementById("bpmn-download-png-button").addEventListener("click",function(e){
			downloadPNG();
		});
		
		document.getElementById("bpmn-toggle-panel-button").addEventListener("click",function(e){
			togglePanel();
		});
		
		document.getElementById("edit-rules-button").addEventListener("click",function(e){
			toggleBpmnRules();
			newRules({resource: po.rulesResource, key: po.rulesKey});
		});
		
		document.getElementById("bpmn-upload").addEventListener("change",function(e){
			openDiagram(e.target.files[0]);
			e.target.value = null;
		});
		
		document.getElementById("panel-close-button").addEventListener("click",function(e){
			closePanel();
		});
		
		document.getElementById("panel-sticky-button").addEventListener("click",function(e){
			document.getElementById("panel-sticky-icon").classList.toggle("active");
			settings.panel.sticky = !settings.panel.sticky;
			saveSettings(settings);
		});
		
		if(settings.panel.sticky) {
			document.getElementById("panel-sticky-icon").classList.add("active");
		} else {
			document.getElementById("panel-sticky-icon").classList.remove("active");
		}
		
		// Rules: Add event listeners
		document.getElementById("rules-bpmn-toggle-button").addEventListener("click",function(e){
			toggleBpmnRules();
		});
		
		document.getElementById("rules-new-button").addEventListener("click",function(e){
			wipeRules();
		});
		
		document.getElementById("rules-add-button").addEventListener("click",function(e){
			newRules();
		});
		
		document.getElementById("rules-open-button").addEventListener("click",function(e){
			document.getElementById("rules-upload").click();
		});
		
		document.getElementById("rules-preview-button").addEventListener("click",function(e){
			previewConfig();
		});
		
		document.getElementById("rules-download-button").addEventListener("click",function(e){
			downloadConfig();
		});
		
		document.getElementById("rules-upload").addEventListener("change",function(e){
			openRules(e.target.files[0]);
			e.target.value = null;
		});
		
		// BPMN: Bootstrap new diagram
		newDiagram();
	}
};