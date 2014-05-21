$('document').ready(function(){	
	getTimeTableToString();
	getDatelist();
	$('#inpStunden').val($('#numberVorlesungsstunden').val());
	//Erlaubt das Auswählen mehrer Zellen der Stundentabelle (Multi-Select für Stundentabelle)
	$(function (){
		var isMouseDown = false;
		var isHighlighted;
		$('#timeTableBody td').mousedown(function(){
			isMouseDown = true;
			$(this).toggleClass("tdSelect");
			isHighlighted = $(this).hasClass("tdSelect");
			return false;
		}).mouseover(function(){
			if(isMouseDown){
				$(this).toggleClass("tdSelect",isHighlighted);
			}
		}).bind("selectstart",function(){
			return false;
		})
		$(document).mouseup(function(){
			isMouseDown= false;			
		});
	});
	//Erlaubt das Auswählen mehrer Zellen der Ausnahmedatenliste
	$('#ulDateList li').each(function(){
		$(this).on('click',function(){
			if($(this).hasClass("tdSelect")){
				$(this).removeClass();
			}
			else{
				$(this).removeClass();
				$(this).addClass("tdSelect");
			}
		});
	});
	//Löscht die ausgewählten Daten aus der Ausnahmendatenliste
	$('#btnDeleteDate').on('click',function(){
		var obj=getSelectedLi();
		for(var i in obj){
			obj[i].remove();
		}
		getTimeTableToString();
		getDatelist();
		$('#inpStunden').val($('#numberVorlesungsstunden').val());
		$('#inpChanged').val(1);
	});
	//Ändert die Anzahl der Vorlesungsstunden
	$('#numberVorlesungsstunden').change(function(){
		getTimeTableToString();
		getDatelist();
		$('#inpStunden').val($(this).val());
		$('#inpChanged').val(1);
	});
	//Markiert alle ausgewÃ¤hlten Zellen mit +
	$('#btnPlus').on('click',function(){
		var obj=getSelectedTd();
		for(var i in obj){
			obj[i].removeClass();
			obj[i].addClass('tdPlus');
			obj[i].html('+');
		}
		getTimeTableToString();
		getDatelist();
		$('#inpStunden').val($('#numberVorlesungsstunden').val());
		$('#inpChanged').val(1);
	});
	//Markiert alle ausgewÃ¤hlten Zellen mit -
	$('#btnMinus').on('click',function(){
		var obj=getSelectedTd();
		for(var i in obj){
			obj[i].removeClass();
			obj[i].addClass('tdMinus');
			obj[i].html('-');
		}
		getTimeTableToString();
		getDatelist();
		$('#inpStunden').val($('#numberVorlesungsstunden').val());
		$('#inpChanged').val(1);
	});
	//Macht alle ausgewÃ¤hlten Zellen leer
	$('#btnClear').on('click',function(){
		var obj=getSelectedTd();
		for(var i in obj){
			obj[i].removeClass();
			obj[i].addClass('tdNeutral');
			obj[i].html('');
		}		
		getTimeTableToString();
		getDatelist();
		$('#inpStunden').val($('#numberVorlesungsstunden').val());
		$('#inpChanged').val(1);
	});
	//Datum der Liste hinzufÃ¼gen, falls noch nicht vorhanden
	$('#btnSetDate').on('click',function(){
		var write= true;
		var value=$('#inpDatepicker').val();
		$('#ulDateList li').each(function(){
			if($(this).html() == value){
				write= false;
				alert("Datum: "+$(this).html()+" ist bereits vorhanden!");
			}
		});
		if(write ==true){
			var item=$('<li>'+value+'</li>');
			$('#ulDateList').append(item);	
			item.on('click',function(){
				if($(this).hasClass("tdSelect")){
					$(this).removeClass();
				}
				else{
					$(this).removeClass();
					$(this).addClass("tdSelect");
				}
			});
			getTimeTableToString();
			getDatelist();
			$('#inpStunden').val($('#numberVorlesungsstunden').val());
			$('#inpChanged').val(1);
		}
	});			
});
//Gibt alle ausgewÃ¤hlten Zellen der Stundentabelle
function getSelectedTd(){
	var counter=0;
	var selectedTds=new Array();
	$('#timeTableBody td').each(function(){
		if($(this).hasClass("tdSelect")){
			selectedTds[counter]=$(this);
			counter++;
		}
	});
	return selectedTds;
}
//Gibt alle ausgewÃ¤hlten Zellen der Ausnahmedatenliste
function getSelectedLi(){
	var counter=0;
	var selectedLis=new Array();
	$('#ulDateList li').each(function(){
		if($(this).hasClass("tdSelect")){
			selectedLis[counter]=$(this);
			counter++;
		}
	});
	return selectedLis;
}
//Holt die Daten der Ausnahmendatenliste
function getDatelist(){
	var dateArray=new Array();
	var counter=0;
	$('#ulDateList li').each(function(){
		dateArray[counter]=$(this).html();
		counter++;
	});
	$('#inpAusnahmen').val(dateArray);
}
//Methode liest Daten der Stundentabelle
function getTimeTableToString(){
	var tempVal;	//temporärer Wert
	var counter;	//Zähler um Zeitraum der Stundentabelle zu zählen (z.B. 8.00-18.00 = 10)
	var str='';
	//Start Uhrzeit der Tabelle ( 8.00-9.00 = 8)
	var startTime = parseInt($('#timeTableBody tr').first().find('th').html().split('.')[0]);
	//Schleife zählt Wochentage durch (So bis Mo)
	//Beginnt bei 1, da Montag Spalte 2
	for(var i=1;i<8;i++){
		counter=0;
		//Schleife zählt von 0.00 Uhr bis Start der Stundentabelle (z.B. 8.00 Uhr)
		//und füllt 
		for(var j=0;j<startTime;j++){
			str+='0';
		}
		$('#timeTableBody').find('tr').find('td:nth-child('+i+')').each(function(){				
			if($(this).html() == '+'){
				tempVal =2;
			}
			else if($(this).html() == '-'){
				tempVal =0;
			}
			else{
				tempVal =1;
			}
			str+=''+tempVal;
			counter++;
		});
		counter+=startTime;
		for(var l=counter;l<24;l++){
			str+='0';
		}
	}
	$('#inpTimeTable').val(str);
}
