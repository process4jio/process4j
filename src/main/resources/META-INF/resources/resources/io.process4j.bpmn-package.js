const P4J_PKG = {
	name: "Process4j",
	uri: "http://bpmn.process4j.io",
	prefix: "p4j",
	xml: {
		tagAlias: "lowerCase"
	},
	types: [
		{	
			name: "Implementation",
			properties: [
				{
					name: "id",
					type: "String",
					isAttr: true
				}
			]
		},
		{	
			name: "While",
			properties: [
				{
					name: "id",
					type: "String",
					isAttr: true
				}
			]
		},
		{	
			name: "Foreach",
			properties: [
				{
					name: "id",
					type: "String",
					isAttr: true
				}
			]
		}
	]
}