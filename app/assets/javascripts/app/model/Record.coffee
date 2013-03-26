Ext.define('DnsManagement.model.Record',
	extend: 'Ext.data.Model'

	fields: [
      name: 'id'
      type: 'int'
    ,
      name: 'name'
      type: 'string'
    ,
      name: 'domainId'
      type: 'int'
    ,
      name: 'recordType'
      type: 'string'
    ,
      name: 'content'
      type: 'string'
    ,
      name: 'ttl'
      type: 'int'
    ,
      name: 'priority'
      type: 'string'
    ,
      name: 'changeDate'
      type: 'string'
	]
)