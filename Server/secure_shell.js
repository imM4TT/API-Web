// Auteur MATTEI Paul
// Script relatif à l'établissement d'une connexion SSH sur une adresse publique
// Dernière Modification 23/06/2020

const host = "serveo.net"; // hote principal
const port = 3000;
const subdomain = "androscope"; // spus domaine de l'url



module.exports = 
{
    // Fonction : Connexion SSH
	connect: async function setup_ssh()
	{
		console.log('');
		console.log("2) Connexion SSH vers l'adresse public du serveur".bold);
		
		var x = await set_tunnel();
		return x;
	},
	port: port
};


function set_tunnel()
{
	const exec = require('child_process').exec;
	let shell = exec('ssh -R '+subdomain+':443:localhost:'+port+' '+host);     //serveo
    //let shell = exec('ssh -R 80:localhost:3000 ssh.localhost.run');          //alternative
	var nb_connexion = 0;

	return new Promise(result =>
	{
        let rep = {"code":0,"message":""};
		shell.stdout.on('data', (data)=>
		{
			if(nb_connexion != 0)
                return;
            nb_connexion ++;
            let adr = "a";

            if(data !== null){
                if(data.includes("m4tt"))
                    adr = data.split(" ")[4];
                else
                    adr = "http://androscope.serveousercontent.com/";
             
                rep.code = "200 OK"; rep.message = "Serveur online."
                
                console.log("Connexion réussie. Le serveur est accessible publiquement via: ".green + adr.cyan);
                
                if(!data.includes("m4tt"))
                    console.log(data);
                result(rep);
            }
            else{
                console.log("Echec de connexion".red);
                rep.code = "500 ERROR"; rep.message = "Serveur offline."

                result(rep);
            }
		});
		shell.stderr.on('data', (data)=>
		{
			if(nb_connexion != 0)
				return;
			if(!data.includes("Pseudo-terminal"))
			{
				console.log(data.red);
                rep.code = "500 ERROR"; rep.message = "Serveur offline."

                result(rep);
			}
		});

	});
}