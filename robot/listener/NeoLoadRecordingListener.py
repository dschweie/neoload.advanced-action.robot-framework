import requests
import os.path
import tempfile
from robot.libraries.BuiltIn import BuiltIn


class NeoLoadRecordingListener:
  """!
    @brief    listener with which one or more test cases can be recorded.

    Neoload is a load and performance testing tool and therefore works on the
    network protocol. By contrast, functional tests generally use actions on
    the web interface, for example. This makes it clear that a test case in
    Robot Framework is very different from one in NeoLoad.

    A test case can be executed by the listener and recorded by NeoLoad at the
    same time. This considerably reduces the development time for performance
    testing.
  """

  ##  Specifications from the Robot Framework for identifying the type of listener
  ROBOT_LISTENER_API_VERSION = 2

  rbt_frmwrk_ignore_libraries = ["Browser", "BuiltIn", "Collections", "DateTime", "Dialogs", "OperatingSystem", "Process", "Screenshot", "String", "Telnet", "XML"]

  ##
  #   @brief      The URL for the data exchange service is stored in the attribute.
  #
  #   The service that transfers external data to NeoLoad runs on the
  #   controller and the script runs on a load generator. They do not
  #   necessarily have to run on the same computer. For this reason, the
  #   attribute is created by the constructor and is then valid for the entire
  #   session.
  endpoint_url = ""
  ##
  #   @brief      The status of the connection to the service is stored in the attribute.
  #
  #   The constructor of the class attempts to establish a connection to the
  #   Data Exchange Service. If this is successful, this attribute is changed
  #   to “CONNECTED” and serves as an indicator for the other methods as to
  #   whether metrics can be sent.
  neoload_status = "UNDEFINED"

  neoload_api_key = ""

  neoload_data = { }

  neoload_host = "localhost"

  neoload_container_elements = []

  def __init__(self, host="localhost", port=7400, apikey="", logfile="listen.txt"):
    """!
      @brief    Constructor of the listener

      @param  host    The name or address of the computer on which an instance
                      of NeoLoad is running must be entered in the parameter.
                      The default value is “localhost” and it is assumed that
                      NeoLoad is running on the computer on which
                      Robot Framework is also running.
      @param  port    The parameter for the port on which the NeoLoad Service
                      is listening must be transferred.
                      The default value is 7400.
      @param  apikey  If the NeoLoad service requires an API key for
                      authentication, this is to be passed in this parameter.
    """
    #path = os.path.join(tempfile.gettempdir(), logfile)
    #self.file = open(path, 'w')
    #self.file.write("__init__ <%s>\n" % (self.neoload_status))

    self.neoload_host = host

    self.endpoint_url = "http://" + host + ":" + str(port) + "/Design/v1/Service.svc"
    self.neoload_api_key = apikey
    try:
      self.neoload_get_status()
    except:
      pass

  def start_suite(self, name, attrs):
    if "READY" == self.neoload_status:
      self.neoload_get_recorder_settings()
      BuiltIn().set_global_variable("&{PROXY}", "server=" + self.neoload_host + ":" + str(self.neoload_data["d"]["ProxySettings"]["Port"]))

  def end_suite(self, name, attrs):
    pass

  def start_test(self, name, attrs):
    """!
      1. Prüfen, ob NeoLoad erreichbar ist und den Status "READY" hat
      2. Prüfen, ob bereits ein UserPath existiert und nach einem erlaubten suchen
      3. Starten der Aufnahme
    """
    try:
      if "READY" == self.neoload_get_status():
        self.neoload_start_recording(name)
        self.neoload_virtual_user = self.neoload_data["d"]["VirtualUser"]
      self.neoload_get_recording_status()
      self.neoload_get_recorder_settings()
    except:
      pass
  
  def end_test(self, name, attrs):
    #self.neoload_stop_recording(name=self.neoload_virtual_user)
    try:
      self.neoload_stop_recording()
    except:
      pass
  
  def start_keyword(self, name, attrs):
    try:
      if "SETUP" == attrs['type']:
        self.neoload_set_base_container("Init")
        self.neoload_container_elements.append(attrs["kwname"])
        self.neoload_set_container(' + '.join(self.neoload_container_elements))
      elif "KEYWORD" == attrs['type']:
        if attrs['libname'] not in self.rbt_frmwrk_ignore_libraries:
          self.neoload_container_elements.append(attrs["kwname"])
          self.neoload_set_container(' + '.join(self.neoload_container_elements))
      elif "TEARDOWN" == attrs['type']:
        self.neoload_set_base_container("End")
        self.neoload_container_elements.append(attrs["kwname"])
        self.neoload_set_container(' + '.join(self.neoload_container_elements))
    except:
      pass

  def end_keyword(self, name, attrs):
    try:
      if "SETUP" == attrs['type']:
        self.neoload_set_base_container("Actions")
      if attrs["kwname"] == self.neoload_container_elements[-1]:
        self.neoload_container_elements.pop()
    except:
      pass
	
  def close(self):
    #self.file.close()
    pass

  def neoload_create_project(self, name, directory="", overwrite=False):
    data = {"d": { }}
    data["d"]["Name"] = name
    if "" != directory:
      data["d"]["DirectoryPath"] = directory
    data["d"]["Overwrite"] = overwrite
    self.neoload_post_request("CreateProject", data)

  def neoload_open_project(self, filepath):
    data = {"d": { }}
    data["d"]["FilePath"] = filepath
    self.neoload_post_request("OpenProject", data)

  def neoload_save_project(self):
    data = {"d": { }}
    self.neoload_post_request("SaveProject", data)

  def neoload_save_as_project(self, name, directory="", overwrite=False, forcestop=False):
    data = {"d": { }}
    data["d"]["Name"] = name
    if "" != directory:
      data["d"]["DirectoryPath"] = directory
    data["d"]["Overwrite"] = overwrite
    data["d"]["ForceStop"] = forcestop
    self.neoload_post_request("SaveAsProject", data)

  def neoload_close_project(self, save=True, forcestop=False):
    data = {"d": { }}
    data["d"]["Save"] = save
    data["d"]["ForceStop"] = forcestop
    self.neoload_post_request("CloseProject", data)

  def neoload_get_status(self):
    data = {"d": { }}
    self.neoload_post_request("GetStatus", data)
    if "201" == str(self.neoload_status_code):
      self.neoload_status = self.neoload_data["d"]["Status"]
    else:
      pass
    return self.neoload_status

  def neoload_contains_user_path(self, name):
    data = {"d": { }}
    data["d"]["Name"] = name
    self.neoload_post_request("ContainsUserPath", data)
  
  def neoload_is_project_open(self, filepath):
    data = {"d": { }}
    data["d"]["FilePath"] = filepath
    self.neoload_post_request("IsProjectOpen", data)

  def neoload_exit(self):
    data = {"d": { }}
    self.neoload_post_request("Exit", data)
  
  def neoload_start_recording(self, userpath, basecontainer="Actions", protocol_websocket=True, protocol_adobeRTMP=False, useragent=None, protocol_sapgui=False, sap_connectionstring="", sap_sessionid=""):
    data = {"d": { }}
    data["d"]["VirtualUser"] = userpath
    data["d"]["BaseContainer"] = basecontainer
    data["d"]["ProtocolWebSocket"] = protocol_websocket
    data["d"]["ProtocolAdobeRTMP"] = protocol_adobeRTMP
    data["d"]["UserAgent"] = useragent
    data["d"]["ProtocolSAPGUI"] = protocol_sapgui
    data["d"]["SAPConnectionString"] = sap_connectionstring
    data["d"]["SAPSessionID"] = sap_sessionid
    self.neoload_post_request("StartRecording", data)

  def neoload_stop_recording(self, frameworkparametersearch=False, genericparametersearch=False, name="", matchingthreshold=60, updatesharedcontainers=False, includevariables=True, deleterecording=False ):
    data = {"d": { }}
    data["d"]["FrameworkParameterSearch"] = frameworkparametersearch
    data["d"]["GenericParameterSearch"] = genericparametersearch
    data["d"]["Name"] = name
    data["d"]["MatchingThreshold"] = matchingthreshold
    data["d"]["UpdateSharedContainers"] = updatesharedcontainers
    data["d"]["IncludeVariables"] = includevariables
    data["d"]["DeleteRecording"] = deleterecording
    self.neoload_post_request("StopRecording", data)

  def neoload_set_container(self, name):
    data = {"d": { }}
    data["d"]["Name"] = name
    self.neoload_post_request("SetContainer", data)

  def neoload_pause_recording(self):
    data = {"d": { }}
    self.neoload_post_request("PauseRecording", data)

  def neoload_resume_recording(self):
    data = {"d": { }}
    self.neoload_post_request("ResumeRecording", data)

  def neoload_set_base_container(self, name="Actions"):
    data = {"d": { }}
    data["d"]["Name"] = name
    self.neoload_post_request("SetBaseContainer", data)

  def neoload_get_recorder_settings(self):
    data = {"d": { }}
    self.neoload_post_request("GetRecorderSettings", data)

  def neoload_get_recording_status(self):
    data = {"d": { }}
    self.neoload_post_request("GetRecordingStatus", data)

  def neoload_post_request(self, command, data):
    #self.file.write("=== %s " % (command))
    if "" != str(self.neoload_api_key):
      data["d"]["ApiKey"] = self.neoload_api_key
    response = requests.post(self.endpoint_url + "/" + command, json=data )
    self.neoload_status_code = response.status_code
    #self.file.write(" (%s) \n" % (response.status_code))
    #self.file.write("    data:     %s\n" % (str(data)))
    if "201" == str(response.status_code):
      self.neoload_data = response.json()
      #self.file.write("    response: %s\n" % (str(response.json())))
    else:
      pass
    #self.file.write("    confirm:  %s\n" % (self.neoload_data))
