package com.example.helloworld.presentations.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloworld.presentations.list.StoryVM

@Composable
fun StoryCard(
    story: StoryVM,
    onDeleteClick: (StoryVM) -> Unit,
    onEditClick: (StoryVM) -> Unit,
    onDetailClick: (StoryVM) -> Unit,
) {
    Box(modifier = Modifier
        .padding(2.dp)
    ) {
        ElevatedCard(
            modifier =
            Modifier.border(
                    width = 3.dp,
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(size = 15.dp)
                )
                .padding(3.dp)
                .width(320.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = story.priority?.backgroundColor ?: Color.White,
                contentColor = Color.White
            ),
        ) {
            Row  (
                modifier = Modifier.height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = story.title,
                    modifier = Modifier.offset(x = (-10).dp, y = (-11).dp)
                        .padding(horizontal = 8.dp)
                        .background(Color.White, RoundedCornerShape(bottomEnd = 12.dp))
                        .padding(8.dp),

                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF303030),
                        textAlign = TextAlign.Start,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (story.done) {
                    Column (
                        modifier = Modifier.padding(8.dp)
                            .offset(y = (-10).dp, x = (-20).dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center)
                    {
                        IconButton(
                            onClick = { onDeleteClick(story) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                }else {
                    Column (
                        modifier = Modifier.padding(8.dp)
                            .offset(y = (-10).dp, x = (-20).dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center

                    )
                    {
                        IconButton(
                            onClick = { onEditClick(story) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            Column  {
                Text(
                    text = story.description ?: "",
                    modifier = Modifier
                        .offset(x = 7.dp)
                        .padding(8.dp),
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = story.priority?.foregroundColor ?: Color.White

                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row (
                    modifier = Modifier.height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(
                        modifier = Modifier
                            .offset(x = 10.dp),
                        border = BorderStroke(2.dp, Color.White),
                        label = { Text(text = story.date) },
                        onClick = { },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF303030),
                            labelColor = Color.White
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Localized description",
                                Modifier.size(AssistChipDefaults.IconSize),
                                tint = story.priority?.backgroundColor ?: Color.White
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        modifier = Modifier
                            .offset(x = 10.dp),
                        border = BorderStroke(2.dp, Color.White),
                        label = { Text(text = story.time) },
                        onClick = { },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF303030),
                            labelColor = Color.White
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Localized description",
                                Modifier.size(AssistChipDefaults.IconSize),
                                tint = story.priority?.backgroundColor ?: Color.White
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )

                    IconButton(
                        onClick = { onDetailClick(story) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Details",
                            tint = Color.Black
                        )
                    }
                }
            }
            if (story.done) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                        {
                    Text(
                        text = "Done",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color(0xFF303030),
                            textAlign = TextAlign.End,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(8.dp)
                        )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check",
                        tint = Color(0xFF303030),
                    )
                }
            }else {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = "Not Done",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color(0xFF303030),
                            textAlign = TextAlign.End,
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
