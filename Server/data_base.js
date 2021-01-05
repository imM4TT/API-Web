// Auteur MATTEI Paul
// Script qui contient toutes les fonctions relatives à la base de données
// Dernière Modification 23/06/2020

const mysql = require('mysql'); // module mysql
const host = 'localhost';         //localhost // ip local d'un pc
const user = "androscope_guest";
const pass = "androscope_pass";
const name = "androscope_db"; 
const port = 3306;
var con_bdd; // objet de connexion mysql
const table_etudiant = 'etudiant';const table_groupe = "groupe";const table_photo = "photo";const table_poursuite = "poursuite"; //nom des tables
	
module.exports = //module exportés qui sont réutilisé dans serveur.js
{
    
    // Fonction : Connexion à la BDD
	connect: async function connexion_bdd()
	{
		console.log("3) Connexion à la BDD".bold);
		
		con_bdd = mysql.createConnection(
		{
			host     : host,
			database : name,
			user     : user,
			password : pass,
			port     : port,
			connectTimeout: 5000,
            multipleStatements: true
		});

		var rep = await connect_bdd();
		return rep;
	},
	
    // Fonction : Ajouter/Modifier des données dans la BDD
    // Parametre : Corps de la requête
	set: async function post_bdd(data)
	{   
        var reply = {"code":0, "message":""};
        var user_exist = await is_user_inBDD(data);
        
        reply = set_sql_query(data, user_exist);
        if(reply.code.includes("403"))
            return reply;
        let log = JSON.parse(JSON.stringify(reply.message)).replace(/\s/g, '#').replace(/photo='.*'/i, "[image]").replace(/\#/g, " ");

        console.log("    <sql-query>".bold.yellow+ JSON.stringify(log));
        
        reply = await post_on_db(reply.message, user_exist);
        
        return reply;
	},
	
    // Fonction : Selectionne(Consulter) les données dans la BDD
    // Parametre : Corps de la requête
	get: async function select_bdd(data)
	{   
        var reply;
        
		var nb = await get_nb_user();
		
		if(nb.code.includes("404"))
			return "404 ERROR";
		
        reply = await get_sql_data(nb.message);

        return reply;
	},
    
    // Fonction : Suppression de données dans la BDD
    // Parametre : Corps de la requête
    delete: async function delete_bdd(data)
    {
        var reply = {"code":"", "message":""};
        var id;
        if(!data.hasOwnProperty("id")){
            return "404 ERROR";
        }
        id = data.id;
        reply = await new Promise(resultat =>
            {      
                var sql_query = 
                            "DELETE FROM " + table_groupe + " WHERE id_groupe='"+id+"';"+
                            "DELETE FROM " + table_photo + " WHERE id_photo='"+id+"';"+
                            "DELETE FROM " + table_poursuite + " WHERE id_poursuite='"+id+"';"+
                            "DELETE FROM " + table_etudiant + " WHERE id='"+id+"';"
                con_bdd.query(sql_query, function (err, result, fields)
                {
                    if(err)
                    {
                        reply.code = "403 ERROR";
                        reply.message = "Communication avec la BDD impossible.";
                    }
                    else
                    {
                        reply.code = "200 OK";
                        if(result[3].affectedRows != 0)
                            reply.message = "Utilisteur #" + id +" supprimé de la base de donnée.";
                        else
                            reply.message = "Aucun ID correspondant. Echec de la suppression.";
                    }
                   
                    resultat(reply)
                });
            })
        
            return reply;
        
    },
	
    // Fonction : Vérification de l'ID existant dans la BDD
    // Parametre : Corps de la requête
	checkId: function check(data)
	{
		return new Promise(resultat =>
		{
			var id = data.id;
            var reply = {"code":0, "message":""};
            
			con_bdd.query("SELECT count(id) as count FROM "+table_etudiant+" WHERE id='"+id+"'", function (err, result, fields) 
			{
				if (err)
                {
					resultat("error");	
                    reply.code = "403 ERROR";
                    reply.message = "Communication avec la BDD impossible.";
                }
                else
                {
                    reply.code = "200 OK";
                    reply.message = result[0];   
                }
				resultat(reply);
			});
		})
	}
	
};



// Fonction : Ajout / Modification de données
// Parametre : Requête mySQL (String), utilisateur présent dans la base de donnée (boolean)
function post_on_db(sql_query, user_exist)
{
    return new Promise(resultat =>
    {
	   con_bdd.query(sql_query, function (err, result, fields) 
	   {
           var reply ={"code":0, "message":""}
           if (err)
           {
               reply.code = "403 ERROR";
               reply.message = "Aucune données modifiées.";
               resultat(reply);	
           }
           else
           {
               var changed_row=0, warning=0;
               if(result.length !== undefined)
               {
                    for(var i in result)
                    {
                        if(user_exist)
                        {
                            changed_row += result[i].changedRows;
                        }
                        else
                        {
                            changed_row += result[i].affectedRows;
                        }
                        
                        warning += result[i].warningCount;
                    }

                }
                else
                {
                    if(user_exist)
                    {
                        changed_row += result.changedRows;
                    }
                    else
                    {
                        changed_row += result.affectedRows;
                    }
                    
                    warning = result.warningCount;
                }
				reply.code = "200 OK"; 
				if(user_exist)
                {
                    if(changed_row <= 0)
                        reply.message = "Aucune modification établie. \n (" + changed_row + " tables affectée(s), " + warning + " erreur).";
                    else
					   reply.message = "Données d'utilisateur mise à jour. \n (" + changed_row + " tables affectée(s), " + warning + " erreur).";
                }

				else
					reply.message = "Nouvel utilisateur enregistré. \n (" + changed_row + " tables affectée(s), " + warning + " erreur).";
               
               resultat(reply);
           }

	   });
	})
}


// Fonction : Définit une requête mySQL imbriqués(INSERT/UPDATE)
// Parametre : Corps de la requête, utilisateur présent dans la base de donnée (boolean)
function set_sql_query(json, user_exist)
{
    var reply = {"code":0,"message":""}
    var index = 0;var nbItemToChange = 0;
    
    var tab = ['nom', 'prenom', 'email', 'telephone', 'promo',    'id_groupe', 'td', 'tp',  'id_photo', 'photo',   'id_poursuite', 'etude', 'emploi'];

    var tab_etudiant = 5; var tab_groupe = 8; var tab_photo = 10; var tab_poursuite = 13;
    var nb_element = tab.length 
    var sql = "";
    var tabToModify = ""; var nb_toModify_etu = 0,nb_toModify_groupe = 0,nb_toModify_photo = 0,nb_toModify_poursuite = 0;
        
    var nb_element_required_forInsert = 10;//  10 avec photo
    for(var i=0; i < nb_element; i++)
    {
        var item = tab[i];

        if(json.hasOwnProperty(item))
        {
            nbItemToChange ++;
            if(i < tab_etudiant)
            {
                tabToModify += "etudiant+";nb_toModify_etu++;
            }
            else if(i < tab_groupe)
            {
                tabToModify += "groupe+";nb_toModify_groupe++;
            }
            else if(i < tab_photo)
            {
                tabToModify += "photo+";nb_toModify_photo++;
            }
            else if(i < tab_poursuite)
            {
                tabToModify += "poursuite+";nb_toModify_poursuite++;
            }
        }
    }

    if(!user_exist && (nbItemToChange != nb_element_required_forInsert)) // utilisteur inexistant et verification du nombre de champs
    {
        reply.code = "403 ERROR";
        reply.message = "Pour créer un nouvel utilisateur il faut saisir toutes les données.";                                       ///REPLY ERROR
        return reply;
    }
 
    for(var i=0; i < nb_element; i++)
    {
        var item = tab[i];

        if(user_exist)// UPDATE DEBUT DE REQUETE
        {
            if(i == 0 && tabToModify.includes("etudiant"))
                sql += "UPDATE "+table_etudiant+" SET ";
            
            else if(i == tab_etudiant && tabToModify.includes("groupe"))
                sql += "UPDATE "+table_groupe+" SET ";
            
            else if(i == tab_groupe && tabToModify.includes("photo"))
                sql += "UPDATE "+table_photo+" SET ";
            
            else if(i == tab_photo && tabToModify.includes("poursuite"))
                sql += "UPDATE "+table_poursuite+" SET ";
        }
        else//INSERT INTO DEBUT DE REQUETE												
        {
            if(i==0 && tabToModify.includes("etudiant"))
                sql += "INSERT INTO "+table_etudiant+" VALUES ('"+json.id+"', ";
            
            else if(i == tab_etudiant && tabToModify.includes("groupe"))
                sql += "INSERT INTO "+table_groupe+" VALUES ('"+json.id+"', ";
            
            else if(i == tab_groupe && tabToModify.includes("photo"))
                sql += "INSERT INTO "+table_photo+" VALUES ('"+json.id+"', ";
            
            else if(i == tab_photo && tabToModify.includes("poursuite"))
                sql += "INSERT INTO "+table_poursuite+" VALUES ('"+json.id+"', ";
        }

        if(json.hasOwnProperty(item))
        {   
            if(user_exist)//UPDATE
            {
                index ++;

                if(json[item].length > 0)
                    sql += item+"='"+json[item]+"'";
                else
                {
                    sql += item+"=null";
                }
                if(index<nb_toModify_etu || index<nb_toModify_etu+nb_toModify_groupe && index > nb_toModify_etu|| index<nb_toModify_etu+nb_toModify_groupe+nb_toModify_photo && index>nb_toModify_etu+nb_toModify_groupe|| index < nb_toModify_etu+nb_toModify_groupe+nb_toModify_photo+nb_toModify_poursuite && index >nb_toModify_etu+nb_toModify_groupe+nb_toModify_photo)//encore des éléments à mettre dans la requête sql
                {
                   sql += " ,";
                }
                else
                {
                    var id = i < tab_etudiant ? "id":(i<tab_groupe ? "id_groupe" : i<tab_photo ? "id_photo": "id_poursuite");

                    sql += " WHERE "+id+"='"+json['id']+"'; ";
                }
            }
            else// INSERT INTO
            {
                index ++;

                if(json[item] != undefined && json[item] != null  && json[item].length > 0)
                    sql += "'"+json[item]+"'";
                else
                {
                   sql += "null";
                }
                if(index<nb_toModify_etu || index<nb_toModify_etu+nb_toModify_groupe && index > nb_toModify_etu|| index<nb_toModify_etu+nb_toModify_groupe+nb_toModify_photo && index>nb_toModify_etu+nb_toModify_groupe|| index < nb_toModify_etu+nb_toModify_groupe+nb_toModify_photo+nb_toModify_poursuite && index >nb_toModify_etu+nb_toModify_groupe+nb_toModify_photo)//encore des éléments à mettre dans la requête sql
                {
                   sql += ", ";
                }
                else
                {
                    sql += ");";
                }
            }


        }
        
    }
    reply.code = "200 OK"; reply.message=sql;                               ///REPLY OK
    return reply;         
}


// Fonction : Selection (Consulter) des données
// Parametre : Nombre d'utilisateur présent dans la BDD (integer)
async function get_sql_data(nbUser)
{
	var reply = {"code":"", "message":{"users":[]}}
	reply.message.users = new Array(nbUser);

	var allUser = await get_all_tables();
	
	if(allUser.code.includes("ERROR"))
		return "404 ERROR";
	
	reply.code = allUser.code;
	
	for(var i=0; i<nbUser; i++)
	{
		reply.message.users[i] ={"nom":"", "prenom":"", "contact":"", "photo":"", "etudiant":"", "ancien":""};
        var n = allUser.data["etudiant"][i].nom !== undefined ? allUser.data["etudiant"][i].nom : "";
        var p = allUser.data["etudiant"][i].prenom !== undefined ? allUser.data["etudiant"][i].prenom : "";
        var ph = typeof (allUser.data["photo"][i].photo)!== undefined ? allUser.data["photo"][i].photo  : "";
        
        var e = allUser.data["etudiant"][i].email !== undefined ? allUser.data["etudiant"][i].email : "";
        var t = allUser.data["etudiant"][i].telephone !== undefined ? allUser.data["etudiant"][i].telephone : "";
        
        var pr = allUser.data["etudiant"][i].promo !== undefined ? allUser.data["etudiant"][i].promo : "";
        var td = allUser.data["groupe"][i].td !== undefined ? allUser.data["groupe"][i].td  : "";
        var tp = allUser.data["groupe"][i].tp !== undefined ? allUser.data["groupe"][i].tp  : "";
        var et = allUser.data["poursuite"][i].etude !== undefined ? allUser.data["poursuite"][i].etude  : "";
        
        var emp = allUser.data["poursuite"][i].emploi !== undefined ?allUser.data["poursuite"][i].emploi : "";
        
        
		reply.message.users[i].nom  = n;
		reply.message.users[i].prenom  = p;
        reply.message.users[i].photo  = ph;
        
		reply.message.users[i].contact  = e + "\n" + "\n"+ t;
        
		reply.message.users[i].etudiant  = "Promo: " + pr + "\n" + "TD:"+td + " TP:" + tp + "\n" + "\n Poursuite: \n"+ et; 
        
		reply.message.users[i].ancien  = "Métier \n" + emp;
	}
	
	return reply;
}


// Fonction : Définit si l'utilisateur est présent dans la BDD avec l'ID présent dans la requête
// Parametre : Corps de la requête
function is_user_inBDD(data)
{
    return new Promise(resultat =>
    {
        var id = data.hasOwnProperty("id") ? data.id : null;
	   con_bdd.query("SELECT count(id) as count FROM "+table_etudiant+" WHERE id='"+id+"'", function (err, result, fields) 
	   {
           if (err)
            resultat("999");	
           
           var res = result[0].count.toString();
           var already_registered = res.localeCompare("0") == 0 ? false : true;
           resultat(already_registered);
	   });
	})
}


// Fonction : Connexion à la BDD
// Parametre : Corps de la requête, utilisateur présent dans la base de donnée (boolean)
function connect_bdd()
{
	return new Promise(resultat =>
	{
        let reply = {"code":0, "message":""};
		con_bdd.connect(function(err) 
		{
			if (err)
			{
				console.log(err +'Erreur de connexion à la base de données'.red);
                reply.code = "500 ERROR"; reply.message = "Base de données offline.";
				resultat(reply);
			}
			else
			{
				console.log('Connexion à la base de données réussie'.green);					
                reply.code = "200 OK"; reply.message = "Base de données online.";
				resultat(reply);
			}
		});
			
	});
}


// Fonction : Définit le nombre d'utilisateur dans la BDD
function get_nb_user()
{	
	return new Promise(resultat =>
	{
		con_bdd.query("SELECT count(*) as count from " + table_etudiant, function (err, result, fields) 
		{
			var reply = {"code":0,"message":""};

			if (err)
			{
				reply.code += "403 ERROR";	
			}
			else
			{
				reply.code += "200 OK";  
				reply.message = result[0].count;
			}
			resultat(reply);
		});
	});
}

// Fonction : Récupère toutes les infoatmrions des 4 tables dans la BDD
async function get_all_tables()
{

		var rep = {"code":"", "data":{"etudiant":[],"poursuite":[],"groupe":[],"photo":[]}}
		var etu = await new Promise(resultat =>
		{
			con_bdd.query("SELECT * from "+table_etudiant, function (err, result, fields) 
			{
				if (err)
				{
					rep.code += "403 ERROR+";	
				}
				else
				{
					rep.code += "200 OK+";  
					rep.data["etudiant"] = result;
				}
				resultat(0);
			});
		});
	
		var poursuite = await new Promise(resultat =>
		{
			con_bdd.query("SELECT * from "+table_poursuite, function (err, result, fields) 
			{
				if (err)
				{
					rep.code += "403 ERROR+";	
				}
				else
				{
					rep.code += "200 OK+";  
					rep.data["poursuite"] = result;
				}
				resultat(0);
			});
		});
		
		var groupe = await new Promise(resultat =>
		{
			con_bdd.query("SELECT * from "+table_groupe, function (err, result, fields) 
			{
				if (err)
				{
					rep.code += "403 ERROR+";	
				}
				else
				{
					rep.code += "200 OK+";  
					rep.data["groupe"] = result;
				}
				resultat(0);
			});
		});
		
		var photo = await new Promise(resultat =>
		{
			con_bdd.query("SELECT CONVERT(photo USING utf8) as photo FROM "+table_photo, function (err, result, fields) 
			{
				if (err)
				{
					rep.code += "403 ERROR+";	
				}
				else
				{
					rep.code += "200 OK+";  
					rep.data["photo"] = result;
				}
				resultat(0);
			});
		});
		return rep;

}