<?php
//liste over mulige hendelser
$possible_url = array("hent_liste", "legg_til_turmaal");
//retur variabel
$returverdi = "En feil oppstod.";

//Database tilkoblings info
$url = "localhost";
$bruker = "u142840";
$passord = "pw.142840";
$db = "db142840";

//Etabler kobling til MySQL-databasen
$dblink = mysqli_connect($url, $bruker, $passord, $db);
mysqli_set_charset($dblink, 'utf8');

//Sjekk om mottatt request er en GET med riktige aksjon-parametre
if (isset($_GET["aksjon"]) && in_array($_GET["aksjon"], $possible_url)) {
    switch ($_GET["aksjon"]) {
        case "hent_liste": //Finn og returner liste over turmål
            $returverdi = hent_liste($dblink);
            break;
        case "legg_til_turmaal":     //Legg til et turmål i databasen
            if (isset($_GET["navn"],$_GET["type"],$_GET["beskrivelse"],$_GET["bilde"],$_GET["latitude"],$_GET["longitude"],$_GET["hoyde"],$_GET["bruker"]))
                $returverdi = legg_til_turmaal($dblink, $_GET["navn"],$_GET["type"],$_GET["beskrivelse"],$_GET["bilde"],$_GET["latitude"],$_GET["longitude"],$_GET["hoyde"],$_GET["bruker"]);
            else
                $returverdi = "URL mangler en variabel";
            break;
    }
}

mysqli_close($dblink);   //Lukk databasekoblingen

exit(json_encode($returverdi, JSON_UNESCAPED_UNICODE));  //Returner data til klienten som en JSON array

// Funksjon som henter liste over turmål
function hent_liste($dblink)
{
    $sql = "SELECT * FROM turmaal";
    $svar = mysqli_query($dblink, $sql);
    $liste = array();
    $rekke = 0;
    while ($rad = mysqli_fetch_assoc($svar)) {
        $liste[$rekke] = $rad;
        $rekke++;
    }
    return $liste;
}

//funksjon som legger til et turmål i databasen
function legg_til_turmaal($dblink, $navn, $type, $beskrivelse, $bilde, $latitude, $longitude, $hoyde, $bruker){
    $sql = "CALL legg_til_turmaal('$navn','$type','$beskrivelse','$bilde','$latitude','$longitude','$hoyde','$bruker')";
    return mysqli_query($dblink,$sql);
}

?>