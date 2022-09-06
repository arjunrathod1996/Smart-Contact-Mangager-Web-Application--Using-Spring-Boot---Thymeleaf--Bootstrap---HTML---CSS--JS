console.log("This is Script File");

const toggleSidebar = ()=>{

    if($(".sidebar").is(":visible")){
        /band karna hai/ 
        /* true */
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
        


    }else{
        /show karna hai/
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }

};