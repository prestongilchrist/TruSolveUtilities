Advanced JSON Reference (AJR) Implementation
Please refer to https://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03 for more details on the core JSON Reference implementation.  Refer to the JSON Pointer specification (https://tools.ietf.org/html/rfc6901) for details on how to use JSON pointers.  All features mentioned in this document should function with the Advanced JSON Reference implementation.  The notable distinction is that the AJR process will, by default, replace references to other documents with the content from the referenced document.

<ol>
	<li>AJR Keywords - all AJR keywords are used within the JSON object (or its descendents) in which the "$ref" is present.</li>
	<ol>
		<li>$ref</li>
		<ol>
			<li>This defines the JSON source that will be included in place of the $ref.</li>
			<li>The value of the $ref in AJR may be either a string (as it is in the core implementation with the same meaning) or a JSON array of strings.</li>
			<li>If $ref is a string</li>
			<ol>
				<li>Behavior is the same as defined int he JSON Reference spec with the only exception being</li>
				<ol>
					<li>Modifications based on other AJR keywords listed in this document</li>
					<li>If other key/value pairs exist in the same object as the $ref, then they are merged with the reference.</li>
				</ol>
			</ol>
			<li>If $ref is a JSON array</li>
			<ol>
				<li>All references within the array are merged together in place of the ref</li>
				<li>Any other key/value pairs that exist in the same object as the $ref are merged</li>
				<li>Where conflicts exist, the $ref object takes priority followed by the array objects in order.</li>
			</ol>
		</ol>
		<li>$refIgnore</li>
		<ol>
			<li>If this value is set, then it is enforced.  If it is not set it is not enforced.</li>
			<li>Causes the system to cease processing of this $ref and the object in which the $ref exists remains as is in the JSON document</li>
			<li>The "$refIgnore" key/value is removed from the resulting document.</li>
		</ol>
		<li>$refDeep</li>
		<ol>
			<li>If this value is set, then it is enforced.  If it is not set it is not enforced.</li>
			<li>Causes the merge operation between the key/value pairs within the $ref parent object to be merged depth-wise with the referenced objects.</li>
			<li>Only JSON Objects are merged.</li>
			<li>JSON Arrays and atomic values are set based on priority ($ref parent object first, then each referenced document)</li>
		</ol>
		<li>$refLocalize</li>
		<ol>
			<li>If this value is set, then it is enforced.  If it is not set it is not enforced.</li>
			<li>Causes the referenced JSON entity to be brought into the local document with the same path in which it exists in the source document.</li>
			<li>The reference is replaced with a document relative (ie #/definitions/foo) $ref to the imported entity.</li>
		</ol>
		<li>$refInline</li>
		<ol>
			<li>If this is set then it will cause a local reference to be incorporated inline where the reference exists.  If the reference is not a local reference, this directive will have no effect.
		</ol>
		<li>$refExcludes</li>
		<ol>
			<li>Optionally set to exclude certain items from the ref that is included.
			<li>This value holds an array of strings. 
			<li>If parsing a reference object, all keys that are present in the excludes array will be excluded from the final document
			<li>Must be placed at the same JSON object level as the attribute/value pairs that you wish to exclude.
		</ol>
		<li>$refArrayProcessing</li>
		<ol>
			<li>This should be set to an object with the keys matching the values of keys of Arrays in the current object that the user wishes to control array merging on.
			<li>Each key will then contain a set fo the following processing directives to use on the named Array
			<ol>
				<li>$refArrayRemovePartialMatch</li>
				<ol>
					<li>The values of this key should be an array of Json nodes (any type) that the user wishes to remove from the source array before MERGING it with the target array.  Behavior will depend on the object type.</li>
					<ol>
						<li>Json Object - If all of the values in the Json Object exist in the source document, this will be considered a match and that matching array element WILL NOT be merged
						<li>Json Array - All JSON array values must match the target.  If this is true, the matching object WILL NOT be merged.
						<li>Json Value - value must match exactly, if true the matching object WILL NOT be merged.
					</ol>
				</ol>
				<li>$refSetMerge - is this key is present (value does not matter), then only values in the source document that DO NOT exist in the target with a "loose match" (where all keys in the source exist in the target) will be merged.</li>
			</ol>
		</ol>
	</ol>
	<li>$ref value formats</li>
	<ol>
		<li>Fully Qualified URL</li>
		<ol>
			<li>{ "$ref": "https://foo.example.com/path/to/json/jsondocumentname.json#/json/pointer/path"} </li>
			<li>This format references a static path of a JSON document published on a web site.</li>
		</ol>
		<li>Maven Dependency Format</li>
		<ol>
			<li>{"$ref": "%%maven.dependency.{groupId}.{artifactId}.json.fileUrl%%#/json/pointer/path"}</li>
			<li>This feature is implemented through ant filtersets within the api-contract-cfg project.  It translates these tokens into the fileUrl format of the dependency location (typically in the local Maven repository).</li>
			<li>The Maven dependency MUST be included in the pom.xml for this JSON document's project.</li>
			<li>The maven dependency will be pulled into the local repository and referenced in this document.</li>
			<li>The dependency MUST exist in either the local repo or a repo available to the project.</li>
		</ol>
		<li>Local Document URL</li>
		<ol>
			<li>{"$ref": "#/json/pointer/path"}</li>
			<li>Refer to JSON Reference and JSON Pointer documentation for behavior.</li>
			<li>Local references within the source document are not modified.</li>
			<li>Local references within included documents are dealt with as follows</li>
			<ol>
				<li>The referenced object entity is created in the source document as the same path as it exists in the included document.</li>
				<li>The local reference is included unchanged in the source document (referencing the now localized entity)</li>
			</ol>
		</ol>
	</ol>
	<li>The build will dereference all documents and publish the dereferenced JSON and YAML artifacts.</li>
	<li>A release process must be run to publish a release version of the Swagger document.</li>
<ol>