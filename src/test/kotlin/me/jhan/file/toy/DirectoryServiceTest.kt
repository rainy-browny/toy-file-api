package me.jhan.file.toy

import me.jhan.file.toy.service.DirectoryService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DirectoryServiceTest(
    @Autowired val directoryService: DirectoryService
) {
    
    private fun removeDirectories(vararg dirList: String) {
        dirList.forEach {
            directoryService.deleteDirectory(it).block();
        }
    }
    
    @Test
    fun preventMoveDirectoryHavingSubDirectory() {
        assertThrows<IllegalArgumentException> {
            directoryService.createDirectory("/hoho/wow").block();
            directoryService.moveDirectory("/hoho", "/hoho2").block();
        }
        
        removeDirectories("/hoho/wow", "/hoho")
    }
    
    @Test
    fun moveAnotherDirectoryTest() {
        directoryService.createDirectory("/hoho/wow2").block();
        directoryService.createDirectory("/hoho/wow3").block();
        directoryService.moveDirectory("/hoho/wow2", "/hoho/wow3/wow2").block();
        
        assert(directoryService.getDirectory("/hoho")
            .block()?.subDirectory?.containsKey("wow2") == false);
        assert(directoryService.getDirectory("/hoho/wow3")
            .block()?.subDirectory?.containsKey("wow2") == true);
        
        removeDirectories("/hoho/wow3/wow2", "/hoho/wow3", "/hoho")
    }
    
    
    @Test
    fun createAndDeleteTest() {
        directoryService.createDirectory("/ww").block();
        assert(directoryService.getDirectory("/ww").block() != null);
        directoryService.deleteDirectory("/ww").block();
        assert(directoryService.getDirectory("/ww").block() == null);
    }
}