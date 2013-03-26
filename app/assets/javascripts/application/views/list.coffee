# Render items list table with header and row section.
Mooml.register 'items-list', (result) ->
  console.log('view: items-list', result)
  console.log('view: result.header', result.header)
  console.log('view: result.items', result.items)
  table { 'class': 'items-list' },
    caption result.caption
    thead {}, Mooml.render 'items-list-header', result.header
    tbody {}, Mooml.render 'items-list-row', result.items
    
# Render items list header section.
Mooml.register 'items-list-header', (item) ->
  tr {},
    th value for value in item

# Render items list row section.
Mooml.register 'items-list-row', (item) ->
  tr {},
    td value for value in item

# Simple items list with button.
Mooml.register 'buttoned-items-list', (result) ->
  console.log('view: button-items-list', result)
  [ Mooml.render('items-list', result),
    button { id: 'button' } ]