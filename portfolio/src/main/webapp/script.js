// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
	const facts =
		['🏙️: I am from Mexico City (capital of Mexico)', '🏎️: I am an avid Formula 1 fan',
		'🏫: I graduated from highschool a year early', 
		'🎓: I was at Stanford for a year with both of my older siblings',
		'🚣‍♂️: I was a competitive rower in high school'];

	// Pick a random fact.
	const fact = facts[Math.floor(Math.random() * facts.length)];

	// Add the fact tp the stage.
	const factContainer = document.getElementById('fact-container');
	factContainer.innerText = fact;
}

let i = 0;
let j = 0;

const PHRASES = ["software engineer", "designer", "product manager", 
               "photographer", "proud Mexican", "entrepreneur"];
const EMOJIS = ["🖥️", "🎨", "💼", "📷", "🇲🇽", "📈"];
const RATE = 100;
const PAUSE = 1000;

/**
 * Fills in the 'I am a' sentences with different phrases
 */
function writeSnippets() {
    if (i == 0) document.getElementById("text").innerHTML = EMOJIS[j] + " I am a ";

	// Use typing effect to print PHRASE[j]
	if (i < PHRASES[j].length) {
        document.getElementById("text").innerHTML += PHRASES[j].charAt(i);
		i++;
		setTimeout(writeSnippets, RATE);
	} 
	
	// Go to next phrase after entire phrase has been printer
	else {
		i = 0;
        (j + 1) < PHRASES.length ? j++ : j = 0;
		setTimeout(writeSnippets, PAUSE);
	}
}

/**
 * The JS necessary for the tabs function to work 
 */
function openCity(evt, sectionName) {
	var i, tabcontent, tablinks;
	tabcontent = document.getElementsByClassName("tabcontent");
	for (i = 0; i < tabcontent.length; i++) {
		tabcontent[i].style.display = "none";
	}
	tablinks = document.getElementsByClassName("tablinks");
	for (i = 0; i < tablinks.length; i++) {
		tablinks[i].className = tablinks[i].className.replace(" active", "");
	}
	document.getElementById(sectionName).style.display = "block";
	evt.currentTarget.className += " active";
}

/**
 * Init function to perform certain tasks onload
 */
window.addEventListener("load", myInit, true); 

function myInit(){
    document.getElementById("defaultOpen").click();
    writeSnippets();
}