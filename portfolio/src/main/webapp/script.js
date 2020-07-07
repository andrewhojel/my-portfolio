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
 * Gets the authentication status of user and other relevant user info 
 */

async function getUser() {
    const response = await fetch('/auth');
    const currentUser = await response.json();
    if (currentUser.loggedIn && currentUser.nickname == "") {
        currentUser.nickname = prompt("Please enter your desired display name: ");
        const params = new URLSearchParams();
        params.append('nickname', currentUser.nickname);
        await fetch('/auth', {method: 'POST', body: params});
    }
    return currentUser;
}

async function changeName() {
    let nickname = prompt("Please enter your desired display name: ");
    const params = new URLSearchParams();
    params.append('nickname', nickname);
    await fetch('/auth', {method: 'POST', body: params});
    
    // Reload the page to update User JSON 
    window.location.reload();
}

/**
 * Get all comments from the server
 */
async function getComments() {
    // Clear old comments
    const commentsEl = document.getElementById('comment_list');
    // for some reason this wasn't working $("comment_list").empty();
    emptyElement(commentsEl);

    // Determine display preferences
    const numComments = document.getElementById('comment_count').value;
    const sortType = document.getElementById('comment_sorting').value;

    // Get comments from the server
    const queryString = '?count=' + numComments + '&sort=' + sortType;
    const response = await fetch('/data' + queryString);
    const comments = await response.json();

    // Display the comments
    comments.forEach((comment) => {
        commentsEl.appendChild(createCommentElement(comment));
    });
}

/**
 * Deletes all children of a certain element
 */
function emptyElement(element) {
    while (element.lastChild) {
        element.removeChild(element.lastChild);
    }
}

/**
 * Creates one comment element
 */
function createCommentElement(comment) {
    const commentElement = document.createElement('li');
    commentElement.classList.add('comment');

    const titleElement = document.createElement('span');
    titleElement.classList.add("comment_title");
    titleElement.innerText = comment.name;

    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.classList.add("comment_button", "delete_one");
    const deleteButtonIcon = document.createElement('i');
    deleteButtonIcon.classList.add("fa", "fa-times");
    deleteButtonElement.appendChild(deleteButtonIcon);
    deleteButtonElement.addEventListener('click', () => {
        deleteComment(comment);

        // Remove the task from the DOM.
        commentElement.remove();
    });

    const breakElement = document.createElement("div");
    breakElement.classList.add("comment_break");

    const contentElement = document.createElement('span');
    contentElement.innerText = comment.comment;

    commentElement.appendChild(titleElement);
    commentElement.appendChild(deleteButtonElement);
    commentElement.appendChild(breakElement);
    commentElement.appendChild(contentElement);
    return commentElement;
}

/**
 * Checks url and clicks the proper tags
 */
const TAB_BUTTON_ID = "commentsOpen";
function clickTab() {
  if (window.location.hash == "#Comments") {
    document.getElementById(TAB_BUTTON_ID).click();
  } else {
    document.getElementById("defaultOpen").click();
  }
}

/** 
 * Tells the server to delete all tasks
 */
const COMMENT_SENTINEL = -1; 
async function deleteAllComments() {
  const params = new URLSearchParams();
  params.append('id', COMMENT_SENTINEL);
  await fetch('/delete-comment', {method: 'POST', body: params});
  getComments();
}

/** 
 * Tells the server to delete the task. 
 */
async function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  await fetch('/delete-comment', {method: 'POST', body: params});
  getComments();

}

/**
 * Ensures that only logged in users can see the comment section!
 */
function toggleCommentSection(user) {
    var commentSection = document.getElementById("authorized_comments");
    var commentBlocker = document.getElementById("unauthorized_comments");
    if (user.loggedIn) {
        commentBlocker.style.display = "none";
        commentSection.style.display = "block";

        // Populate the logout button with a link
        var logoutButton = document.getElementById("logout");
        logoutButton.setAttribute("href", user.logoutURL);

        // Populate form with name options
        var nameSelect = document.getElementById("comment_name");

        const emailOption = document.createElement("option");
        emailOption.innerText = user.email;
        emailOption.setAttribute("value", user.email);

        const nicknameOption = document.createElement("option");
        nicknameOption.innerText = user.nickname;
        nicknameOption.setAttribute("value", user.nickname);

        nameSelect.appendChild(nicknameOption);
        nameSelect.appendChild(emailOption);

        getComments();
    } else {
        commentSection.style.display = "none";
        commentBlocker.style.display = "block";

        // make log in button 
        const loginButton = document.createElement('a')
        loginButton.setAttribute("href", user.loginURL);
        loginButton.innerText = "Log in";
        loginButton.classList.add("link");

        // attach to parent nodes
        commentBlocker.appendChild(loginButton);
    }
}

// location data for all content
const markerData =  [new google.maps.LatLng(37.422179, -122.084036), 
    new google.maps.LatLng(33.809218, -118.066596),
    new google.maps.LatLng(37.428214, -122.161195),
    new google.maps.LatLng(37.425333, -122.170153),
    new google.maps.LatLng(37.430174, -122.173341),
    new google.maps.LatLng(42.383904, -71.133425),
    new google.maps.LatLng(19.419853, -99.161854),
    new google.maps.LatLng(37.427474, -122.169901),
    new google.maps.LatLng(30.302448, -97.748066),
    new google.maps.LatLng(44.061074, -70.532018),
    new google.maps.LatLng(42.360162, -71.094203),
    new google.maps.LatLng(39.952391, -75.193171)]

/**
 * Generates the map & markers
 */
function createMap() {
    var styledMapType = new google.maps.StyledMapType(
        mapJSON,
        {name: 'My Map'});

    const map = new google.maps.Map(
        document.getElementById('map'),
        {center: {lat: 37.693622, lng: -97.195151}, zoom: 3,
        mapTypeControlOptions: {
                mapTypeIds: ['My Map']
            }
        });
  
    map.mapTypes.set('My Map', styledMapType);
    map.setMapTypeId('My Map');

    // gather HTML for marker content & generate markers
    prepareMarkers();
    for (let i = 0; i < markersHTML.length; i++) {
        addMarker(map, i);
    }
}

/**
 * Adds content to a marker and adds to map
 */
function addMarker(map, i) {
    // add marker content
    var infowindow = new google.maps.InfoWindow({
        content: markersHTML[i]
    });

    // create marker 
    var marker = new google.maps.Marker({
        position: markerData[i],
        map: map,
        title: 'marker #' + i,
    });

    // show information when clicked
    marker.addListener('click', function() {
        infowindow.open(map, marker);
    });
}

/**
 * Takes content from Experience & Education tabs to populate Map
 */
let markersHTML = [];
function prepareMarkers() {
    // only render the content once
    if (markersHTML.length == 0) {
        let experiences = document.getElementsByClassName("experience");

        // remove formatting used for each experience
        for (let i = 0; i < experiences.length; i++) {
            var contentString = experiences[i].cloneNode(true);
            contentString.classList.remove("experience");
            var experience_text = contentString.getElementsByClassName("experience_text");
            experience_text[0].classList.remove("experience_text");
            var experience_image = contentString.getElementsByClassName("experience_image");
            experience_image[0].classList.replace("experience_image", "marker_image");

            markersHTML[i] = contentString;
        }
    }
}

/**
 * Init function to perform certain tasks onload
 */
window.addEventListener("load", myInit, true); 

async function myInit() {
    const user = await getUser();
    toggleCommentSection(user);
    clickTab();
    writeSnippets();
}

// contains the style formatting for the map feature
const mapJSON =  [
        {
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#f5f5f5"
            }
            ]
        },
        {
            "elementType": "labels.icon",
            "stylers": [
            {
                "visibility": "off"
            }
            ]
        },
        {
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#616161"
            }
            ]
        },
        {
            "elementType": "labels.text.stroke",
            "stylers": [
            {
                "color": "#f5f5f5"
            }
            ]
        },
        {
            "featureType": "administrative.land_parcel",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#bdbdbd"
            }
            ]
        },
        {
            "featureType": "poi",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#eeeeee"
            }
            ]
        },
        {
            "featureType": "poi",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#757575"
            }
            ]
        },
        {
            "featureType": "poi.park",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#e5e5e5"
            }
            ]
        },
        {
            "featureType": "poi.park",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#9e9e9e"
            }
            ]
        },
        {
            "featureType": "road",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#ffffff"
            }
            ]
        },
        {
            "featureType": "road.arterial",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#757575"
            }
            ]
        },
        {
            "featureType": "road.highway",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#dadada"
            }
            ]
        },
        {
            "featureType": "road.highway",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#616161"
            }
            ]
        },
        {
            "featureType": "road.local",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#9e9e9e"
            }
            ]
        },
        {
            "featureType": "transit.line",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#e5e5e5"
            }
            ]
        },
        {
            "featureType": "transit.station",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#eeeeee"
            }
            ]
        },
        {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [
            {
                "color": "#c9c9c9"
            }
            ]
        },
        {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [
            {
                "color": "#9e9e9e"
            }
            ]
        }
        ]