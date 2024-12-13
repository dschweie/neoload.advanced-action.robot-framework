import requests
import time
import calendar
import platform
import copy

class NeoLoadMonitoringListener:
    """!
        @brief    Listener, der die Ausführungszeiten an den NeoLoad Controller übertragen kann
        
        NeoLoad ist ein Last- und Performanztestwerkzeug, das überwiegend auf
        Basis der Netzwerkkommunikation Antwortzeiten ermittelt. 
        
        Wenn aus NeoLoad heraus ein Testfall aufgerufen wird, der mit Robot
        Framework entwickelt wurde, dann kann NeoLoad selbst nur eine Dauer
        messen, die der gesamten Durchführungszeit entspricht.
        
        Mit diesem Listener werden die Durchführungszeiten der einzelnen 
        Test Cases und Keywords an den NeoLoad Controller übermittelt.  
    """

    ##  Vorgaben vom Robot Framework zur Kennzeichnung der Art des Listeners
    ROBOT_LISTENER_API_VERSION = 2

    ##
    #   @brief      In dem Attribut wird die URL für den Data Exchange Service hinterlegt
    #
    #   Der Service, über den External Data an NeoLoad übertragen werden, läuft
    #   auf dem Controller und das Skript läuft auf einem Lastgenerator. Die 
    #   beiden müssen nicht zwangsläufig auf dem gleichen Rechner laufen. Aus
    #   diesem Grund wird das Attribut vom Konstruktor erzeugt und ist dann 
    #   für die gesamte Session gültig.
    endpoint_url = ""
    ##
    #   @brief      In dem Attribut wird der Status der Verbindung zum Service gehalten
    #
    #   Im Konstruktor der Klasse wird versucht, eine Verbindung zum Data 
    #   Exchange Service aufzubauen. Wenn das gelungen ist, dann wird dieses 
    #   Attribut auf "CONNECTED" geändert und dient den anderen Methoden als 
    #   Indikator, ob Metriken gesendet werden können.
    status = "UNDEFINED"

    ##
    #   @brief      Template für den Kontext, in dem die Metriken übertragen werden
    #
    #   Der Kontext ist eine optionale Angabe, die man einer Sitzung mitgeben 
    #   kann. In NeoLoad lassen sich die übertragenen Metriken auf Basis des
    #   Kontext filtern und auswerten.
    #   
    #   Das Template enthält lediglich die Standardwerte, die vom Konstruktor
    #   ergänzt werden. 
    json_context = {"Software" : "Robot Framework"}
    
    ##
    #   @brief      Template für eine Metrik, die an NeoLoad gesendet wird
    #
    #   In diesem Attribut wird ein Template gehalten aus dem dann die konkreten
    #   Entries erzeugt werden, die dann an den NeoLoad Controller übertragen
    #   werden.
    json_entry = {"d":{"SessionId":"", "Unit":"s"}}

    ##
    #   @brief      In diesem Attribut wird die Hierarchie für die Metriken gehalten
    #
    #   Die Metriken für die Keywords, die aus Testfällen heraus aufgerufen 
    #   werden, sollen in einer hierarchischen Darstellung präsentiert werden,
    #   damit die Auswertung etwas leichter fällt.
    #
    #   In diesem Array werden die einzelnen Bestandteile gehalten, aus denen
    #   dann der Pfad zusammengesetzt wird, unter dem der Messwert abzhulegen 
    #   ist.
    list_testcase_path= ["Robot Framework", "Test Cases"]
    
    ##
    #   @brief      Dictonary zur Übersetzung der Status eines Testfalls von Robot Framework zu NeoLoad
    #
    #   NeoLoad kennt für Statusmeldungen nur die Status <code>PASS</code> und
    #   <code>FAIL</code>. Da das Robot Framework zusätzlich noch den Status
    #   <code>SKIP</code> kennt, dient dieses Dictionary zur Übersetzung der 
    #   Status in den NeoLoad Status.
    dict_test_result = {"PASS":"PASS" , "FAIL":"FAIL" , "SKIP":"PASS"}
    ##
    #   @brief      Dictonary zur Übersetzung der Status einer Message von Robot Framework zu NeoLoad
    #
    #   NeoLoad kennt für Statusmeldungen nur die Status <code>PASS</code> und
    #   <code>FAIL</code>. Im Robot Framework kann eine Message mehrere Status 
    #   haben, die in NeoLoad unbekannt sind. Damit ein gültiger Entry 
    #   übertragen wird, werden die Status des Robot Framework mittels des 
    #   Dictionary auf <code>PASS</code> oder <code>FAIL</code> gemappt.
    # 
    #   @remark     Es werden nur Nachrichten an NeoLoad übertragen, die auf
    #               den Status <code>FAIL</code> gemappt sind!
    #   
    #   @see        log_message()
    dict_log_level = {"FAIL":"FAIL" , "WARN":"FAIL" , "INFO":"PASS" , "DEBUG":"PASS" , "TRACE":"PASS"}
    
    def __init__(self, host="localhost", port=7400, script_name=""):
        """!
            @brief    Konstruktor des Listeners
            
            Mit dem Konstruktor wird der Listener bezüglich der Kommunikation
            mit dem NeoLoad Controller initialisiert.
            
            @param  host    In dem Parameter kann der Aufrufer den Hostnamen
                            übergeben, auf dem der NeoLoad Controller läuft.
                            Wird kein Wert übergeben, dann hat der Parameter
                            den Wert 'localhost'.
            @param  port    In dem Parameter ist der Port zu übergeben, auf
                            dem das Data Exchange API auf Nachrichten horcht.
                            Der Defaultwert ist 7400.
        """
        self.endpoint_url = "http://" + host + ":" + str(port) + "/DataExchange/v1/Service.svc"
        self.json_context["Os"] = platform.platform()
        self.json_context["Location"] = platform.node()
        self.json_context["Script"] = script_name
        response = requests.post(self.endpoint_url + "/Session", json={"d": {"Context": self.json_context}} )
        if "201" == str(response.status_code):
            data = response.json()
            self.json_entry["d"]["SessionId"] = data["d"]["SessionId"]
            self.status = "CONNECTED"
        else:
            pass
    
    def start_suite(self, name, attrs):
        """!
            @brief    Die Methode pflegt die Liste für die Hierarchie der Test Cases
            
            Damit die Messwerte zu den einzelnen Test Cases eindeutig zugeordnet 
            werden können, werden die Testsuiten als Ordner über den Test Case
            gesetzt. Damit lassen sich die Test Cases mit gleichem Namen in 
            unterschiedlichen Testsuiten sauber unterscheiden.
            
            Die Darstellung in einer Baumdarstellung entspricht damit auch der 
            Darstellung im Logfile vom Robot Framework.
            
            @param  name    In diesem Parameter übergibt das Robot Framework
                            den Namen der Testsuite, die gestartet wird.
            @param  attrs   In diesem Parameter übergibt das Robot Framework
                            zusätzliche Informationen im Zusammenhang mit
                            der Testsuite, die gestartet wird.
        """
        self.list_testcase_path.append(name)

    def end_suite(self, name, attrs):
        """!
            @brief    Die Methode pflegt die Liste für die Hierarchie der Test Cases
            
            Damit die Messwerte zu den einzelnen Test Cases eindeutig zugeordnet 
            werden können, werden die Testsuiten als Ordner über den Test Case
            gesetzt. Damit lassen sich die Test Cases mit gleichem Namen in 
            unterschiedlichen Testsuiten sauber unterscheiden.
            
            Die Darstellung in einer Baumdarstellung entspricht damit auch der 
            Darstellung im Logfile vom Robot Framework.
            
            @remark         Die Methode liefert bewusst keine Messwerte, da aus
                            Sicht eines Performanztest die Durchführungszeit auf
                            Ebene der Testsuite nicht von Interesse ist.
            
            @param  name    In diesem Parameter übergibt das Robot Framework
                            den Namen der Testsuite, die beendet wird.
            @param  attrs   In diesem Parameter übergibt das Robot Framework
                            zusätzliche Informationen im Zusammenhang mit
                            der Testsuite, die beendet wird.
        """
        self.list_testcase_path.pop()

    def start_test(self, name, attrs):
        """!
            @brief    Die Methode pflegt die Liste für die Hierarchie der Test Cases
            
            Damit die Messwerte zu den einzelnen Test Cases eindeutig zugeordnet 
            werden können, werden die Testsuiten als Ordner über den Test Case
            gesetzt. Damit lassen sich die Test Cases mit gleichem Namen in 
            unterschiedlichen Testsuiten sauber unterscheiden.
            
            Die Darstellung in einer Baumdarstellung entspricht damit auch der 
            Darstellung im Logfile vom Robot Framework.
            
            @param  name    In diesem Parameter übergibt das Robot Framework
                            den Namen des Test Case, die gestartet wird.
            @param  attrs   In diesem Parameter übergibt das Robot Framework
                            zusätzliche Informationen im Zusammenhang mit
                            dem Test Case, der gestartet wird.
        """
        self.list_testcase_path.append(name)
    
    def end_test(self, name, attrs):
        """!
            @brief    Die Methode informiert den Controller über die Ergebnisse des Test Case
            
            Wenn ein Testfall endet, dann soll der Controller die Information
            zur Durchführungszeit und auch zum Durchführungsergebnis erhalten.
            
            @param  name    In diesem Parameter übergibt das Robot Framework
                            den Namen des Test Case, die beendet wird.
            @param  attrs   In diesem Parameter übergibt das Robot Framework
                            zusätzliche Informationen im Zusammenhang mit
                            dem Test Case, der beendet wird.
        """
        entry = copy.deepcopy(self.json_entry)
        entry["d"]["Path"] = self.buildTestCasePath()
        entry["d"]["Value"] = attrs['elapsedtime'] / 1000
        entry["d"]["Timestamp"] = str( int(round(time.time() * 1000)) ) 
        entry["d"]["Status"] = {"Message": attrs['message'], "State":  self.dict_test_result[attrs['status']]}
        if "CONNECTED" == self.status:
            response = requests.post(self.endpoint_url + "/Entry", json=entry)
        self.list_testcase_path.pop()

    def start_keyword(self, name, attrs):
        """!
            @brief    Die Methode pflegt die Liste für die Hierarchie
            
            Damit die Messwerte zu den einzelnen Test Cases eindeutig zugeordnet 
            werden können, werden die Testsuiten als Ordner über den Test Case
            gesetzt. Damit lassen sich die Test Cases mit gleichem Namen in 
            unterschiedlichen Testsuiten sauber unterscheiden.
            
            Die Darstellung in einer Baumdarstellung entspricht damit auch der 
            Darstellung im Logfile vom Robot Framework.
            
            @param  name    In diesem Parameter übergibt das Robot Framework
                            den Namen des Keywords, das gestartet wird.
            @param  attrs   In diesem Parameter übergibt das Robot Framework
                            zusätzliche Informationen im Zusammenhang mit
                            dem Keyword, das gestartet wird.
        """
        if "Keyword" != str(attrs['type']):
            self.list_testcase_path.append(attrs['type'])
        self.list_testcase_path.append(name)
         
    def end_keyword(self, name, attrs):
        """!
            @brief    Die Methode informiert den Controller über die Ergebnisse eines Keywords
            
            Das Keyword ist die kleinste Einheit, in die man ein Robot Framework
            Skript auflösen kann. Das Keyword kann dabei unterschiedliche 
            Funktionen haben, die auch in der Ergebnisdarstellung auffindbar 
            sein soll. So kann das Keyword ein Testschritt innerhalb eines 
            Testfalls sein und auf der anderen Seite kann ein Keyword auch 
            von einem anderen Keyword aufgerufen werden. In diesem Fall ist
            es eher ein Aufruf einer Hilfsfunktion.
            
            Um beiden Themen gerecht zu werden, werden am Ende eines Keywords
            zwei Entries erzeugt, die an den NeoLoad Controller gesendet werden.
            
            Der erste Eintrag steht für das Keyword selbst. Wenn ein Keyword
            häufiger genutzt wird, dann ist von Interesse, wie die Performance
            über die Zeit hinweg für genau dieses Keyword war. Unterhalb des 
            Pfades "Robot Framework/Keywords" werden die Messwerte nach Library
            und Keyword sortiert abgelegt.
            
            Unterhalb des Pfades "Robot Framework/Test Cases" werden Knoten in
            der Struktur angelegt, die der Anwender auch aus den Logfiles vom
            Robot Framework kennt. Diese Struktur ist im Grund auch mit der von
            NeoLoad vergleichbar und somit lassen sich Container und Keywords
            unter Umständen auch miteinander vergleichen.
            
            Im Unterschied zum Robot Framework erhält der Anwender über die 
            External Data keine Information zur Dauer der Setup- und Teardown-
            Knoten. Die Dauer kann aber im Grund durch die Keywords abgeleitet
            werden, da in der Regel nur ein Keyword aufgerufen wird, das dann
            weitere Aufrufe beinhaltet kann. Die Dauer dieses Keywords wird als
            Metrik bereitgestellt.
            
            @param  name    In diesem Parameter übergibt das Robot Framework
                            den Namen des Keywords, das beendet wird.
            @param  attrs   In diesem Parameter übergibt das Robot Framework
                            zusätzliche Informationen im Zusammenhang mit
                            dem Keyword, das beendet wird.
        """
        entry_keyword = self.build_current_entry(str("Robot Framework|Keywords|" + name).replace(".", "|"), attrs['elapsedtime'] / 1000, "s")
        entry_teststep = self.build_current_entry(self.buildTestCasePath(), attrs['elapsedtime'] / 1000, "s")
        jsonResult = {"d": {"results": [entry_keyword, entry_teststep]}}
        if "CONNECTED" == self.status:
            response = requests.post(self.endpoint_url + "/Entries", json=jsonResult)
        #
        # Hierarchie has to be managed    
        self.list_testcase_path.pop()
        if "Keyword" != str(attrs['type']):
            self.list_testcase_path.pop()

    def log_message(self, message):
        """!
            @brief    Diese Methode soll Fehler aus dem Robot Framework an den NeoLoad Controller melden.
            
            Diese Methode überträgt eine Message aus dem Robot Framework, wenn
            es sich um eine Fehlermeldung handelt.
            
            Da NeoLoad nur zwischen PASS und FAIL unterscheiden kann, wird 
            über das Dictionary dict_log_level eine Übersetzung der Log Levels
            konfiguriert.

            @param  message     In dem Parameter übergibt das Robot Framework
                                einen Satz von Informationen zu der Nachricht.
        """
        if "FAIL" == str(self.dict_log_level[message["level"]]): 
            entry = self.build_current_entry("Robot Framework|Log|" + str(message["level"]), 1, "", self.dict_log_level[message["level"]], message["message"])
            entry["Url"] = self.list_testcase_path[-1]
            json_entry = {"d": entry}
            json_entry["d"]["Status"]["Code"] = "RF-" + message["level"]
            if "CONNECTED" == self.status:
                response = requests.post(self.endpoint_url + "/Entry", json=json_entry)

    def buildTestCasePath(self) -> str:
        """!
            @brief    Die Methode baut aus den aktuellen Informationen zur Hierarchie den zugehörigen Path für NeoLoad
            
            Diese Methode ist eine Hilfsfunktion, die aus der Liste zur 
            Bildung der Hierarchie einen String generiert, der von NeoLoad als
            Pfad für die Metrik interpretiert wird.
            
            @return   Die Methode liefert einen String zurück, in dem die 
                      Hierachieebenen durch senkrechte Striche getrennt sind.
        """
        entryPath = ""
        for i in range(len(self.list_testcase_path)):
            if 0 == i:
                entryPath = self.list_testcase_path[i]
            else:
                entryPath = entryPath + "|" + self.list_testcase_path[i]
        return entryPath
    
    def build_current_entry(self, path="", value=0, unit="s", status_state="PASS", status_message=""):
        """!
            @brief    Die Methode hilft bei der Erstellung des Entries, der an NeoLoad gesendet werden soll.
            
            Diese Methode baut einen Entry mit den wesentlichen Werten zusammen,
            der dann an NeoLoad übergeben werden kann.
            
            @param  path                In dem Parameter ist ein Strung zu 
                                        übergeben, der für den Pfad steht, unter
                                        dem der Eintrag in NeoLoad zur Anzeige
                                        gebracht wird.
                                        Dieser Wert ist durch den Aufrufer zu
                                        setzen.
            @param  value               In dem Parameter ist der Wert zu 
                                        übergeben, der für den Eintrag 
                                        festgehalten werden soll.
                                        Wird kein Wert übergeben, wird eine 0
                                        eingesetzt.
            @param  unit                In dem Parameter ist die physikalische
                                        Einheit für den übergebenen Wert zu 
                                        übergeben. Wenn es sich um Antwortzeiten
                                        handelt, sollte der Wert in Sekunden 
                                        angegeben werden, da dieses auch die 
                                        übliche Einheit in NeoLoad ist.
            @param  status_state        In diesem Parameter kann ein Kennzeichen
                                        für den Status übergeben werden. 
                                        NeoLoad kennt "PASS" und "FAIL".
                                        Der Defaultwert ist "PASS".
            @param  status_message      In dem Parameter kann der Aufrufer einen
                                        Text übergeben, der als Nachricht in 
                                        NeoLoad abgelegt wird. Diese Information
                                        macht auf jeden Fall Sinn, wenn ein 
                                        Fehler gemeldet werden soll.
        """
        entry = copy.deepcopy(self.json_entry["d"])
        entry["Path"] = str(path)
        entry["Value"] = value
        entry["Unit"] = unit
        entry["Timestamp"] = str( int(round(time.time() * 1000)) ) 
        entry["Status"] = {"State":status_state,  "Message":status_message} 
        return entry
    
