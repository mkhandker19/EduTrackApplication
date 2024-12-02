package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class storageUploader {

    private BlobContainerClient containerClient;

    public storageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=csc311khandkerserver;AccountKey=vmhudKETnstd5FUKdF1UnlCa8cpy4HTBN55momq8rhchC3gQm6EEGICE4/XjIZgIjDruRKNLFhEc+AStuj0BKw==;EndpointSuffix=core.windows.net")
                .containerName("khandkercsc311storage")
                .buildClient();
    }


    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
    }
    public BlobContainerClient getContainerClient(){
        return containerClient;
    }
}