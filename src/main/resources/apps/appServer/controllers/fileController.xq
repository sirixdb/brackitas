(:
 *
 * [New BSD License]
 * Copyright (c) 2011, Brackit Project Team <info@brackit.org>  
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 **
 * 
 * @author Henrique Valer
 * 
 *
:)
module namespace controller="http://brackit.org/lib/appServer/fileController";
import module namespace model="http://brackit.org/lib/appServer/fileModel";
import module namespace view="http://brackit.org/lib/appServer/fileView";
import module namespace appController="http://brackit.org/lib/appServer/appController";

declare function save() as item() {
	let $fPathName := req:getParameter("name"),
		$query := req:getParameter("query")
	return
	    let $msg := 
    		if (xqfile:save($fPathName, $query)) then
    		    view:msgSuccess("Saved sucessfully!")
    		else
                view:msgFailure("Problems while saving..."),
            $success := session:setAttribute("msg",$msg)  
       return
            if ($success) then
                appController:load()
            else
                appController:load()
}; 

declare function compile() as item() {
    let $fPathName := req:getParameter("name"),
        $query := req:getParameter("query")
    return
        let $compilation := xqfile:compile($fPathName, $query),
            $msg := if (fn:string-length($compilation) eq 0) then
                        view:msgSuccess("Compiled sucessfully!")
                    else
                        view:msgFailure($compilation)   
       return
            if (session:setAttribute("msg",$msg)) then
                appController:load()
            else
                appController:load()
};

declare function delete() as item() {
    let $fPathName := req:getParameter("name")
    return
        let $msg := 
            if (xqfile:delete($fPathName)) then
                view:msgSuccess("Deleted sucessfully!")
            else
                view:msgFailure("Deletion failed!")   
       return
            if (session:setAttribute("msg",$msg)) then
                appController:load()
            else
                appController:load()
};

declare function action() as item() {
	let $action := fn:normalize-space(req:getParameter("action"))
	return
		if (fn:compare($action,"save") eq 0) then
			save()
		else 
		    if (fn:compare($action,"compile") eq 0) then
				compile()
			else
			    if (fn:compare($action,"delete") eq 0) then
					delete()
				else
					"ops"
};