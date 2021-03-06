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
module namespace rscController="http://brackit.org/lib/appServer/rscController";
import module namespace rscView="http://brackit.org/lib/appServer/rscView";
import module namespace view="http://brackit.org/lib/appServer/fileView";
import module namespace appView="http://brackit.org/lib/appServer/appView";
import module namespace rscModel="http://brackit.org/lib/appServer/rscModel";

declare function rscController:load() as item() {
    let $app := req:get-parameter("app"),
        $base := req:get-parameter("name"),
        $butClick := req:get-parameter("renBut")
    return
        rscView:fileForm($base,$app)
};

declare function rscController:rename() as item() {
    let $butClick := req:get-parameter("sub"),
        $fPathName := req:get-parameter("name"),
        $app := req:get-parameter("app"),
        $newName := req:get-parameter("newName")
    return
        let 
            $content :=
            if (fn:string-length($butClick) > 0) then
                if (rscModel:validateFileName($newName)) then
                    if (rsc:rename($fPathName,$newName)) then
                        view:msgSuccess("Renamed sucessfully!")
                    else
                        view:msgSuccess("Problems while renaming!")
                else
                    view:msgSuccess("The new name cannot contain space, slash or be empty!")
            else
                rscView:renameFileForm($fPathName,$app)
        return
            appView:menuContent(appView:createMenu($app),$content)            
};
    
declare function rscController:renameFileForm() as item() {
    let $fPathName := req:get-parameter("name"),
        $app := req:get-parameter("app")
    return
        rscView:renameFileForm($fPathName,$app)
};

declare function rscController:delete() as item() {
    let $fPathName := req:get-parameter("name"),
        $app := req:get-parameter("app")
    return
        let $msg := 
            if (rsc:delete($fPathName)) then
                view:msgSuccess("Deleted sucessfully!")
            else
                view:msgSuccess("Deletion failed!")
        return
            appView:menuContent(appView:createMenu($app),$msg)
};

declare function rscController:action() as item() {
    let $action := fn:normalize-space(req:get-parameter("action"))
    return
        if (fn:compare($action,"rename") eq 0) then
            rscController:rename()
        else 
            if (fn:compare($action,"delete") eq 0) then
                rscController:delete()
            else
                "ops"
};