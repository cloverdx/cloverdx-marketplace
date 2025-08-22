function string getFileShareUrl(string storageName, string storageFileAddress, string shareName){
	return "https://"+storageName+"."+storageFileAddress+"/"+shareName;
}

function string getFileShareUrlDirectory(string storageName, string storageFileAddress, string shareName, string path){
	return getFileShareUrl(storageName, storageFileAddress, shareName) + (path!=null?"/"+path:"");
}