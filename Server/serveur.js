// Auteur MATTEI Paul
// Script principal relatif au fonctionnement du serveur
// Dernière Modification 23/06/2020

const express = require('express'); // module express.js
const app = express();

const color = require('colors');var path = require('path');const bodyParser = require('body-parser'); // module de couleur, path, module de parser

var services_state = {"code":0,"message":""};

var connexion_bdd = require("./data_base"); // import des modules personnels
var connexion_remote = require("./secure_shell");// modules personnel

var index_request = 0; // nombre de requete recue au total depuis le lancement du serveur


init_server(); // LANCEMENT du serveur


// Fonction qui s'occupe de la mise en place des routes
function listen_route()
{
	app.get('/', function (req, res) 
	{
		incoming_request("GET", "/");
		res.send('Bienvenue sur le serveur Androscope');
	})
	
	
	app.post('/POST/DATA/', async function (req, res) 
	{
		incoming_request("POST", "/POST/DATA/", req.body); // 1) LOG

        var reply = await connexion_bdd.set(req.body);     // 2) traitement et constitution de la reponse json
        
        reponse_from_server(res, reply, index_request);    // 3) envoie de la reponse
	})
	    
    app.get('/GET/DATA/', async function (req, res) 
	{
		incoming_request("GET", "/GET/DATA/", "Données table MySQL"); // 1) LOG
        
		var reply = await connexion_bdd.get();             // 2) traitement et constitution de la reponse json

		reponse_from_server(res, reply, index_request);    // 3) envoie de la reponse
	})
	
    
    app.post('/DELETE/', async function (req, res) 
	{
		incoming_request("DELETE", "/DELETE/", req.body); // 1) LOG
        
		var reply = await connexion_bdd.delete(req.body); // 2) traitement et constitution de la reponse json
        
		reponse_from_server(res, reply, index_request);   // 3) envoie de la reponse
	})
    
	app.get('/GET/STATUS/', function (req, res) 
	{
		incoming_request("GET", "/STATUS/", "Status serveur");
        
		reponse_from_server(res, services_state, index_request);
	})
	
	app.post('/GET/ID/', async function (req, res) 
	{
		incoming_request("POST", "/POST/ID/", req.body); // LOG

		var reply = await connexion_bdd.checkId(req.body);
        
		reponse_from_server(res, reply, index_request);
	})
	
	
	console.log("LISTEN".green);

}




////////////////////                         \\\\\\\\\\\\\\\\\\
///////////////////   Check Data & Set Reply  \\\\\\\\\\\\\\\\\\
//////////////////  						   \\\\\\\\\\\\\\\\\\

// Fonction : Permet de renvoyer une réponse au client
// Parametre : Objet de communication d'express.js, réponse (json), id de la requête (integer)
function reponse_from_server(res, reply, id)
{
    var code = reply.code;
    code = code.split(" ")[0]
    var msg = reply.message;


	res.status(code).json(
	{
		reply
	});
    
	if(code.includes("200"))
		console.log(('      </Request#' + id  + "> ").bold.yellow  + (code + " ").green + "(reply: "+JSON.stringify(reply.message)+")");
	else
		console.log(('      </Request#' + id  + "> ").bold.yellow  + (code+ " ").red + "(reply: "+JSON.stringify(reply.message)+")");	
}


// Fonction : Permet un debug de l'acheminement d'une requête recue
// Parametre : Type de la requête, URI du serveur, messag de la requête
function incoming_request(type, location, msg)
{
	index_request ++;
    let mssg;
    if(msg)
        mssg = JSON.parse(JSON.stringify(msg));
    var today = new Date();
    var date = today.getDate()+'-'+(today.getMonth()+1)+'-'+today.getFullYear();
    var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
    var dateTime = " ( " + date+' '+time + " )";
    
	console.log('');
	console.log('');
    
    console.log((('<Request#' + index_request + ">").bold.yellow + 'User made a ' + type + ' request on ' + location) + dateTime);
    
    if(type.includes("POST") && mssg.hasOwnProperty("photo"))
    {
        mssg.photo = "[...]("+ msg.photo.length + " octets)"
        console.log("  <parameter> ".bold.yellow + JSON.stringify(mssg));
    }
    else
    {
        console.log("  <parameter> ".bold.yellow + JSON.stringify(mssg));
    }
    if(!msg)
        console.log("  </Request> ".bold.yellow);
}


////////////////////                         \\\\\\\\\\\\\\\\\\
/////////////////// 	Connexion Serveur     \\\\\\\\\\\\\\\\\\
//////////////////  						   \\\\\\\\\\\\\\\\\\


// Fonction : Etablie en 3 étapes la connexion du serveur et son adresse publique
async function connexion()
{
	var un = await connexion_local();
	var deux = await connexion_remote.connect();
	var trois = await connexion_bdd.connect();

    services_state.code = deux.code;
    services_state.message = deux.message + " " + trois.message;
}



async function connexion_local()
{
	var m_rep = await app.listen(connexion_remote.port, function () 
	{
		return new Promise(result =>
		{
			console.log(('1) Le serveur est en écoute sur le port ' + connexion_remote.port).bold);
			listen_route();

		});
    }).on('error', function (err) 
    {
        console.log(('1) Port busy. Serveur off.' + connexion_remote.port).bold);
        return rep;
    });

}



function init_server()
{
    console.log('');
	console.log('Starting Androscope Server'.bold.bgGray);
	console.log('');
	
	app.use(bodyParser.json());
	app.use(bodyParser.urlencoded({extended: true}));
	
	connexion();
}

