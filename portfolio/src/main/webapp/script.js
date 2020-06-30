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

	// Add the fact to the stage.
	const factContainer = document.getElementById('fact-container');
	factContainer.innerText = fact;
}

let i = 0;
let j = 0;

const PHRASES = ["a software engineer", "a designer", "a product manager", 
               "a photographer", "a proud Mexican", "an entrepreneur"];
const EMOJIS = ["🖥️", "🎨", "💼", "📷", "🇲🇽", "📈"];
const RATE = 100;
const PAUSE = 1000;

/**
 * Fills in the 'I am a' sentences with different phrases
 */
function writeSnippets() {
    if (i == 0) document.getElementById("snippetsTarget").innerHTML = EMOJIS[j] + " I am ";

	// Use typing effect to print PHRASE[j]
	if (i < PHRASES[j].length) {
        document.getElementById("snippetsTarget").innerHTML += PHRASES[j].charAt(i);
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
function openTab(evt, sectionName) {
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
 * Fetch function from servlet
 */
async function getWelcomeData() {
    // Get comments from the server
    const response = await fetch('/data');
    let comments = await response.json();

    // Display the comments
    const commentsEl = document.getElementById('comment_list');
    comments.forEach((comment) => {
        commentsEl.appendChild(createCommentElement(comment));
    });
}

/**
 * Creates one comment element
 */
function createCommentElement(comment) {
    const commentElement = document.createElement('li');
    commentElement.className = 'comment';

    const titleElement = document.createElement('span');
    titleElement.className = "comment_title";
    titleElement.innerText = comment.name;

    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.className = "comment_button delete"
    deleteButtonElement.innerHTML = '<i class="fa fa-times"></i>';
    deleteButtonElement.addEventListener('click', () => {
        deleteTask(comment);

        // Remove the task from the DOM.
        commentElement.remove();
    });

    const breakElement = document.createElement("div");
    breakElement.className = "comment_break"

    const contentElement = document.createElement('span');
    contentElement.innerText = comment.comment;

    commentElement.appendChild(titleElement);
    commentElement.appendChild(deleteButtonElement);
    commentElement.appendChild(breakElement);
    commentElement.appendChild(contentElement);
    return commentElement;
}

/**
 * Checks url and redirects to comments tab
 */
const TAB_ID = "Comments"
const TAB_BUTTON_ID = "commentsOpen";
function commentsLoad() {
  if (window.location.href.indexOf("#Comments") > -1) {
    document.getElementById(TAB_BUTTON_ID).click();
  }
}

/** Tells the server to delete the task. */
function deleteTask(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}

/**
 * Init function to perform certain tasks onload
 */
window.addEventListener("load", myInit, true); 

function myInit() {
    document.getElementById("defaultOpen").click();
    writeSnippets();
    commentsLoad();
    getWelcomeData();
}
