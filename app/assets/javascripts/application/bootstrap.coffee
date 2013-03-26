# In order to use Composer.js together with CoffeeScript, we have to
# reference the extend-functions and define them in the global scope.
# This ensures an easy implementation.
#
# Now, we can use CoffeeScript's default class syntax:
#   class MyModel extends Application.Model ...
@Application =
  Collection: Composer.Collection.extend()
  Controller: Composer.Controller.extend()
  Model:      Composer.Model.extend()

# Our custom namespace - call it as you like.
@Custom = {}

# Syncs a model or collection with the server.
#
# This is the central method through which we communicate with the server.
# All objects (either models or collections) call this method in order to
# stay in sync with the server.
Composer.sync = (method, object, options) ->

  # CRUD to HTTP mapping.
  http =
    create: 'post'
    read:   'get'
    update: 'put'
    delete: 'delete'

  # If an object needs to be created or updated, assemble the data. The
  # attributes contained in the 'data' member are written in a namespace
  # defined by the member 'data_key', if set.
  attr = {}
  if method in ['create', 'update']

    # Extract raw data and remove id.
    temp = object.toJSON()
    delete temp[object.id_key]

    # If the member 'data_key' is set, write into separate namespace.
    if object.data_key? then attr[object.data_key] = temp else attr = temp

  # Build the request and send it.
  new Request.JSON

    # Initialize the request.
    method:     http[method]
    url:        object.get_url()
    data:       attr

    # Callbacks for successful and failed requests.
    onSuccess:  options.success
    onError:    options.error

  # After all this configuration, finally send the request.
  .send()