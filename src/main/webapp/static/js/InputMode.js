/* 
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var SUBJECT_TYPES = new Array("su", "sb");
var OBJECT_TYPES = new Array("ou", "ob", "ol");

function generateUUID()
{
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
	return v.toString(16);
    });
}

function cloneUniqueStmt(fieldset, newId)
{
    fieldset.id = "fieldset-" + newId;
    
    // "Remove statement" button

    fieldset.replaceChild(fieldset.children[1], cloneUniqueSubject(fieldset.children[1], newId));
    fieldset.replaceChild(fieldset.children[2], cloneUniqueProperty(fieldset.children[2], newId));
    fieldset.replaceChild(fieldset.children[3], cloneUniqueObject(fieldset.children[3], newId));
    
    return fieldset;
}

function cloneUniqueSubject(controlGroupDiv, newId)
{
    var controlsDiv = controlGroupDiv.children[controlGroupDiv.children.length - 1];

    // tab headings list
    var tabList = controlsDiv.children[0];
    tabList.children[0].id = "li-su-" + newId;
    tabList.children[0].onclick = function() { toggleSubjectTabs("su", newId); };
    tabList.children[1].id = "li-sb-" + newId;
    tabList.children[1].onclick = function() { toggleSubjectTabs("sb", newId); };

    // tab panes
    var suDiv = controlsDiv.children[1];
    suDiv.id = "div-su-" + newId;
    suDiv.children[0].removeAttribute("value");
    var sbDiv = controlsDiv.children[2];
    sbDiv.id = "div-sb-" + newId;
    sbDiv.children[0].removeAttribute("value");
    
    return controlGroupDiv;
}

function cloneUniqueProperty(controlGroupDiv, newId)
{
    var controlsDiv = controlGroupDiv.children[controlGroupDiv.children.length - 1];
    controlsDiv.children[0].id = "pu" + newId;
    controlsDiv.children[0].removeAttribute("value");
    
    return controlGroupDiv;
}

function cloneUniqueObject(controlGroupDiv, newId)
{
    controlGroupDiv.id = "control-group-" + newId;
    controlGroupDiv.removeChild(controlGroupDiv.children[0]); // remove duplicate <label>
	
    var removeButton = controlGroupDiv.children[controlGroupDiv.children.length - 2];
    removeButton.onclick = function() { removeObject(newId); };

    var controlsDiv = controlGroupDiv.children[controlGroupDiv.children.length - 1];
    controlsDiv.id = "controls-" + newId;

    // tab headings list
    var tabList = controlsDiv.children[0];
    tabList.children[0].id = "li-ou-" + newId;
    tabList.children[0].onclick = function() { toggleObjectTabs("ou", newId); };
    tabList.children[1].id = "li-ob-" + newId;
    tabList.children[1].onclick = function() { toggleObjectTabs("ob", newId); };
    tabList.children[2].id = "li-ol-" + newId;
    tabList.children[2].onclick = function() { toggleObjectTabs("ol", newId); };

    // tab panes
    var ouDiv = controlsDiv.children[1];
    ouDiv.id = "div-ou-" + newId;
    ouDiv.children[0].removeAttribute("value");
    var obDiv = controlsDiv.children[2];
    obDiv.id = "div-ob-" + newId;
    obDiv.children[0].removeAttribute("value");
    var olDiv = controlsDiv.children[3];
    olDiv.id = "div-ol-" + newId;
    olDiv.children[0].value = ''; // textarea

    return controlGroupDiv;
}

function removeObject(id)
{
    document.getElementById("control-group-" + id).style.display = 'none';
}

function toggleSubjectTabs(activeType, id)
{
    toggleTabs(SUBJECT_TYPES, activeType, id);
}

function toggleObjectTabs(activeType, id)
{
    toggleTabs(OBJECT_TYPES, activeType, id);
}

function toggleTabs(types, activeType, id)
{
    for (var i = 0; i < types.length; i++)
    {
	var tabListItem = document.getElementById("li-" + types[i] + "-" + id);
	if (activeType === types[i]) tabListItem.className = 'active';
	else tabListItem.className = '';

	var tabPaneDiv = document.getElementById("div-" + types[i] + "-" + id);
	if (activeType === types[i]) tabPaneDiv.style.display = 'block';
	else tabPaneDiv.style.display = 'none';
    }		    
}